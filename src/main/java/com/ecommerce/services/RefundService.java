package com.ecommerce.services;

import com.ecommerce.exceptions.ConflictException;
import com.ecommerce.exceptions.PaymentException;
import com.ecommerce.exceptions.ResourceNotFoundException;
import com.ecommerce.models.DTO.requests.RefundRequest;
import com.ecommerce.models.DTO.responses.RefundResponse;
import com.ecommerce.models.entities.Order;
import com.ecommerce.models.entities.OrderItem;
import com.ecommerce.models.entities.Payment;
import com.ecommerce.models.entities.PaymentRefund;
import com.ecommerce.models.entities.SellerOrder;
import com.ecommerce.models.enums.OrderStatus;
import com.ecommerce.models.enums.PaymentStatus;
import com.ecommerce.models.enums.RefundStatus;
import com.ecommerce.repositories.OrderRepository;
import com.ecommerce.repositories.PaymentRefundRepository;
import com.ecommerce.repositories.PaymentRepository;
import com.ecommerce.repositories.ProductRepository;
import com.ecommerce.utils.OrderStatusCalculator;
import com.stripe.exception.StripeException;
import com.stripe.net.RequestOptions;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentRefundRepository refundRepository;
    private final ProductRepository productRepository;

    // if the order / payment has one of these statuses the customer is allowed to request refund
    private static final Set<OrderStatus> REFUNDABLE_ORDER_STATUSES = EnumSet.of(
            OrderStatus.PAID, OrderStatus.PROCESSING, OrderStatus.SHIPPED, OrderStatus.DELIVERED);

    private static final Set<PaymentStatus> REFUNDABLE_PAYMENT_STATUSES =  EnumSet.of(
            PaymentStatus.SUCCEEDED, PaymentStatus.PARTIALLY_REFUNDED);

    @Transactional
    public RefundResponse refundMine(Long userId, Long orderId, RefundRequest req) {
        Order order = orderRepository.findByIdAndCustomerId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (req.sellerOrderId() != null) {
            // if the user asks for one seller order refund
            SellerOrder target = order.getSellerOrders().stream()
                    .filter(so -> so.getId().equals(req.sellerOrderId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Seller order not found in this order"));
            if (!REFUNDABLE_ORDER_STATUSES.contains(target.getStatus())) {
                throw new ConflictException("This part of the order cannot be refunded");
            }
        // if the user asks for full order refund
        } else if (order.getSellerOrders().stream()
                .noneMatch(so -> REFUNDABLE_ORDER_STATUSES.contains(so.getStatus()))) {
            throw new ConflictException("This order cannot be refunded");
        }

        return refund(orderId, req);
    }

    @Transactional(readOnly = true)
    public Page<RefundResponse> listMine(Long userId, Pageable pageable) {
        return refundRepository.findByPaymentOrderCustomerId(userId, pageable).map(RefundResponse::from);
    }
//Very complicated  when you have a look pls explain to me so we can simplified it


    @Transactional
    public RefundResponse refund(Long orderId, RefundRequest req) {
        // lock order before proceeding with refund
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        Payment payment = paymentRepository
                .findFirstByOrderIdAndStatusInOrderByIdDesc(orderId, REFUNDABLE_PAYMENT_STATUSES)
                .orElseThrow(() ->
                        new ConflictException("Order has no successful payment to refund")
                );

        // checks previous refunds
        BigDecimal alreadyRefunded = refundRepository.sumExcluding(payment.getId(), RefundStatus.FAILED);

        SellerOrder target = null;
        BigDecimal amount;

        if (req.sellerOrderId() != null) {
            // if the user asks for one seller order refund
            target = order.getSellerOrders().stream()
                    .filter(so -> so.getId().equals(req.sellerOrderId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Seller order not found in this order"));
            if (target.getStatus() == OrderStatus.REFUNDED) {
                throw new ConflictException("This part of the order is already refunded");
            }
            // amount is that seller order total
            amount = target.getSubtotal();
        } else {
            // if the user asks for full order refund
            // amount is the full order total that is left un-refunded
            amount = payment.getAmount().subtract(alreadyRefunded);
        }

        // Stripe
        com.stripe.model.Refund stripeRefund = createStripeRefund(payment, order, target, amount);

        // saves the refund in db
        PaymentRefund refund = new PaymentRefund();
        refund.setPayment(payment);
        refund.setSellerOrder(target);
        refund.setAmount(amount);

        refund.setStripeRefundId(stripeRefund.getId());
        if ("succeeded".equals(stripeRefund.getStatus())) {
            refund.setStatus(RefundStatus.SUCCEEDED);
        } else if ("failed".equals(stripeRefund.getStatus())
                || "canceled".equals(stripeRefund.getStatus())) {
            refund.setStatus(RefundStatus.FAILED);
            refund.setFailureReason(stripeRefund.getFailureReason());
        } else {
            refund.setStatus(RefundStatus.PENDING);
        }

        refundRepository.save(refund);

        // reflect changes because of refund in db
        List<SellerOrder> affected = (target != null)
                ? List.of(target)
                : order.getSellerOrders().stream()
                .filter(so -> so.getStatus() != OrderStatus.REFUNDED)
                .toList();

        // restore stock
        for (SellerOrder so : affected) {
            if (so.getStatus() == OrderStatus.PAID || so.getStatus() == OrderStatus.PROCESSING) {
                for (OrderItem item : so.getItems()) {
                    productRepository.increaseStock(item.getProduct().getId(), item.getQuantity());
                }
            }
            // set seller order status
            so.setStatus(OrderStatus.REFUNDED);
        }
        // calculate the total order status based on the singular seller order statuses
        OrderStatusCalculator.recalculate(order);

        // set payment statuses as fully or partially refunded accordingly
        boolean fullyRefunded = alreadyRefunded.add(amount).compareTo(payment.getAmount()) == 0;
        payment.setStatus(fullyRefunded ? PaymentStatus.REFUNDED : PaymentStatus.PARTIALLY_REFUNDED);

        return RefundResponse.from(refund);
    }

    @Transactional(readOnly = true)
    public Page<RefundResponse> list(Pageable pageable) {
        return refundRepository.findAll(pageable).map(RefundResponse::from);
    }

    // Stripe request
    private com.stripe.model.Refund createStripeRefund(Payment payment, Order order,
                                                       SellerOrder target, BigDecimal amount) {
        try {
            long cents = amount.movePointRight(2).longValueExact();

            // create a key to handle duplicates
            String key = "refund-" + payment.getId() + "-"
                    + (target == null ? "all" : target.getId()) + "-" + cents;

            // Stripe refund
            return com.stripe.model.Refund.create(
                    RefundCreateParams.builder()
                            .setPaymentIntent(payment.getStripePaymentIntentId())
                            .setAmount(cents)
                            .putMetadata("orderId", String.valueOf(order.getId()))
                            .build(),
                    RequestOptions.builder().setIdempotencyKey(key).build());
        } catch (StripeException e) {
            throw new PaymentException("Payment provider error: " + e.getMessage());
        }
    }
}
