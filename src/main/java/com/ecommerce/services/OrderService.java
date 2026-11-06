package com.ecommerce.services;

import com.ecommerce.models.DTO.responses.OrderResponse;
import com.ecommerce.models.DTO.responses.OrderSummaryResponse;
import com.ecommerce.models.DTO.responses.SellerOrderDetailResponse;
import com.ecommerce.exceptions.ConflictException;
import com.ecommerce.exceptions.ResourceNotFoundException;
import com.ecommerce.models.entities.Order;
import com.ecommerce.models.entities.OrderItem;
import com.ecommerce.models.entities.SellerOrder;
import com.ecommerce.models.entities.SellerProfile;
import com.ecommerce.models.enums.OrderStatus;
import com.ecommerce.repositories.OrderRepository;
import com.ecommerce.repositories.ProductRepository;
import com.ecommerce.repositories.SellerOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    // the seller is only allowed to change from one step to the next
    private static final Map<OrderStatus, OrderStatus> NEXT_STATUS = Map.of(
            OrderStatus.PAID, OrderStatus.PROCESSING,
            OrderStatus.PROCESSING, OrderStatus.SHIPPED,
            OrderStatus.SHIPPED, OrderStatus.DELIVERED);

    private final OrderRepository orderRepository;
    private final SellerOrderRepository sellerOrderRepository;
    private final ProductRepository productRepository;
    private final SellerService sellerService;
    private final PaymentService paymentService;

    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> listMine(Long userId, Pageable pageable) {
        return orderRepository.findByCustomerId(userId, pageable).map(OrderSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public OrderResponse getMine(Long userId, Long orderId) {
        return OrderResponse.from(findMine(userId, orderId));
    }

    @Transactional
    public OrderResponse cancelMine(Long userId, Long orderId) {
        findMine(userId, orderId);

        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // cannot cancel orders that have been paid
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new ConflictException("Only unpaid orders can be cancelled");
        }

        cancelUnpaid(order);
        return OrderResponse.from(order);
    }

    @Transactional
    public void cancelExpired(Long orderId) {
        orderRepository.findByIdForUpdate(orderId)
                .filter(o -> o.getStatus() == OrderStatus.PENDING_PAYMENT)
                .ifPresent(this::cancelUnpaid);
    }

    @Transactional(readOnly = true)
    public Page<SellerOrderDetailResponse> listForSeller(Long userId, OrderStatus status, Pageable pageable) {
        SellerProfile seller = sellerService.getApprovedSeller(userId);

        Page<SellerOrder> page = (status == null || status == OrderStatus.PENDING_PAYMENT)
                ? sellerOrderRepository.findBySellerIdAndStatusNot(seller.getId(), OrderStatus.PENDING_PAYMENT, pageable)
                : sellerOrderRepository.findBySellerIdAndStatus(seller.getId(), status, pageable);

        return page.map(SellerOrderDetailResponse::from);
    }

    @Transactional(readOnly = true)
    public SellerOrderDetailResponse getForSeller(Long userId, Long sellerOrderId) {
        return SellerOrderDetailResponse.from(findSellerOrder(userId, sellerOrderId));
    }

    @Transactional
    public SellerOrderDetailResponse updateStatus(Long userId, Long sellerOrderId, OrderStatus newStatus) {
        SellerOrder sellerOrder = findSellerOrder(userId, sellerOrderId);

        if (NEXT_STATUS.get(sellerOrder.getStatus()) != newStatus) {
            throw new ConflictException(
                    "Cannot change status from " + sellerOrder.getStatus() + " to " + newStatus);
        }
        sellerOrder.setStatus(newStatus);

        // changes total order depending on the other seller orders (sets the min seller order status as the order status)
        Order order = sellerOrder.getOrder();
        order.getSellerOrders().stream()
                .map(SellerOrder::getStatus)
                .min(Comparator.naturalOrder())
                .ifPresent(order::setStatus);

        return SellerOrderDetailResponse.from(sellerOrder);
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> listAll(OrderStatus status, Pageable pageable) {
        Page<Order> page = (status == null)
                ? orderRepository.findAll(pageable)
                : orderRepository.findByStatus(status, pageable);
        return page.map(OrderSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public OrderResponse getAny(Long orderId) {
        return OrderResponse.from(orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found")));
    }

    private Order findMine(Long userId, Long orderId) {
        return orderRepository.findByIdAndCustomerId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    private SellerOrder findSellerOrder(Long userId, Long sellerOrderId) {
        SellerProfile seller = sellerService.getApprovedSeller(userId);

        SellerOrder sellerOrder = sellerOrderRepository.findByIdAndSellerId(sellerOrderId, seller.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (sellerOrder.getStatus() == OrderStatus.PENDING_PAYMENT) {
            throw new ResourceNotFoundException("Order not found");
        }
        return sellerOrder;
    }

    private void cancelUnpaid(Order order) {
        paymentService.cancelPendingIntents(order.getId());
        order.setStatus(OrderStatus.CANCELLED);
        for (SellerOrder sellerOrder : order.getSellerOrders()) {
            sellerOrder.setStatus(OrderStatus.CANCELLED);
            for (OrderItem item : sellerOrder.getItems()) {
                productRepository.increaseStock(item.getProduct().getId(), item.getQuantity());
            }
        }
    }
}
