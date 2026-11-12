package com.ecommerce.services;

import com.ecommerce.models.DTO.responses.PaymentIntentResponse;
import com.ecommerce.models.DTO.responses.PaymentResponse;
import com.ecommerce.exceptions.BadRequestException;
import com.ecommerce.exceptions.ConflictException;
import com.ecommerce.exceptions.PaymentException;
import com.ecommerce.exceptions.ResourceNotFoundException;
import com.ecommerce.models.entities.Order;
import com.ecommerce.models.entities.Payment;
import com.ecommerce.models.enums.OrderStatus;
import com.ecommerce.models.enums.PaymentStatus;
import com.ecommerce.repositories.OrderRepository;
import com.ecommerce.repositories.PaymentRepository;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PaymentService {

    // PaymentIntent Statuses in Stripe for reusable intent
    private static final Set<String> REUSABLE_STATUSES =
            Set.of("requires_payment_method", "requires_confirmation", "requires_action");

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @Value("${app.currency:eur}")
    private String currency;

    @Transactional(readOnly = true)
    public Page<PaymentResponse> listAll(PaymentStatus status, Pageable pageable) {
        Page<Payment> page = (status == null)
                ? paymentRepository.findAll(pageable)
                : paymentRepository.findByStatus(status, pageable);
        return page.map(PaymentResponse::from);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getAny(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .map(PaymentResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    }

    @Transactional
    public PaymentIntentResponse createIntent(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndCustomerId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new ConflictException("Order is not awaiting payment");
        }

        try {
            // handles duplicates
            Payment pending = paymentRepository
                    .findFirstByOrderIdAndStatusOrderByIdDesc(orderId, PaymentStatus.PENDING)
                    .orElse(null);

            if (pending != null) {
                PaymentIntent existing = PaymentIntent.retrieve(pending.getStripePaymentIntentId());
                if (REUSABLE_STATUSES.contains(existing.getStatus())) {
                    return PaymentIntentResponse.of(pending, existing.getClientSecret());
                }
                pending.setStatus(PaymentStatus.FAILED);
            }

            // Stripe works in cents
            long amount = order.getTotalAmount().movePointRight(2).longValueExact();

            // Stripe class that handles payment process
            PaymentIntent intent = PaymentIntent.create(PaymentIntentCreateParams.builder()
                    .setAmount(amount)
                    .setCurrency(currency)
                    .putMetadata("orderId", String.valueOf(orderId))
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .setAllowRedirects(
                                            PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                    .build())
                    .build());
                    // disable payment methods that require redirecting for simplicity reasons in frontend

            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setStripePaymentIntentId(intent.getId());
            payment.setAmount(order.getTotalAmount());
            payment.setCurrency(currency);
            payment.setStatus(PaymentStatus.PENDING);

            // the clientSecret token is sent to the client, and it allows them to pay for the order through Stripe
            return PaymentIntentResponse.of(paymentRepository.save(payment), intent.getClientSecret());

        } catch (StripeException e) {
            throw new PaymentException("Payment provider error: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public PaymentResponse getMine(Long userId, Long paymentId) {
        return paymentRepository.findByIdAndOrderCustomerId(paymentId, userId)
                .map(PaymentResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    }

    @Transactional
    public void handleWebhook(String payload, String signature) {
        if (signature == null) {
            throw new BadRequestException("Missing Stripe signature");
        }

        // webhook checks if the payment is really paid
        Event event;
        try {
            event = Webhook.constructEvent(payload, signature, webhookSecret);
        } catch (SignatureVerificationException e) {
            throw new BadRequestException("Invalid Stripe signature");
        }

        switch (event.getType()) {
            case "payment_intent.succeeded" -> onSucceeded(extractIntent(event));
            case "payment_intent.payment_failed" -> onFailed(extractIntent(event));
            default -> { }
        }
    }

    @Transactional
    private void onSucceeded(PaymentIntent intent) {
        Payment payment = paymentRepository.findByStripePaymentIntentId(intent.getId()).orElse(null);

        // handle duplicate events
        if (payment == null || payment.getStatus() == PaymentStatus.SUCCEEDED) {
            return;
        }

        Order order = orderRepository.findByIdForUpdate(payment.getOrder().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        payment.setStatus(PaymentStatus.SUCCEEDED);
        payment.setFailureReason(null);

        if (order.getStatus() == OrderStatus.PENDING_PAYMENT) {
            order.setStatus(OrderStatus.PAID);
            order.getSellerOrders().forEach(so -> so.setStatus(OrderStatus.PAID));
        }
    }

    @Transactional
    private void onFailed(PaymentIntent intent) {
        paymentRepository.findByStripePaymentIntentId(intent.getId())
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .ifPresent(p -> {
                    p.setStatus(PaymentStatus.FAILED);
                    p.setFailureReason(intent.getLastPaymentError() != null
                            ? intent.getLastPaymentError().getMessage()
                            : "Payment failed");
                });
    }

    // converts webhook event to a PaymentIntent object
    private PaymentIntent extractIntent(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        try {
            StripeObject object = deserializer.getObject().isPresent()
                    ? deserializer.getObject().get()
                    : deserializer.deserializeUnsafe();
            return (PaymentIntent) object;
        } catch (EventDataObjectDeserializationException e) {
            throw new BadRequestException("Cannot read Stripe event");
        }
    }

    @Transactional
    public void cancelPendingIntents(Long orderId) {
        List<Payment> payments = paymentRepository.findByOrderIdAndStatusIn(
                orderId, List.of(PaymentStatus.PENDING, PaymentStatus.FAILED));

        for (Payment payment : payments) {
            try {
                // get intent from Stripe using stripePaymentIntentId
                PaymentIntent intent = PaymentIntent.retrieve(payment.getStripePaymentIntentId());

                if ("succeeded".equals(intent.getStatus()) || "processing".equals(intent.getStatus())) {
                    throw new ConflictException("Payment is being processed, try again shortly");
                }
                if (!"canceled".equals(intent.getStatus())) {
                    intent.cancel();
                }
            } catch (StripeException e) {
                throw new PaymentException("Payment provider error: " + e.getMessage());
            }

            payment.setStatus(PaymentStatus.CANCELLED);
            payment.setFailureReason("Order cancelled");
        }
    }
}
