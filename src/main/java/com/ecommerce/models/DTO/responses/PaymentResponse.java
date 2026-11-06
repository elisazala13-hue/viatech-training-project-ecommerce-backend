package com.ecommerce.models.DTO.responses;

import com.ecommerce.models.entities.Payment;
import com.ecommerce.models.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// after the payment is handled by stripe, information saved to db
public record PaymentResponse(
        Long id, Long orderId, BigDecimal amount, String currency,
        PaymentStatus status, String failureReason, LocalDateTime createdOn
) {
    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(p.getId(), p.getOrder().getId(), p.getAmount(), p.getCurrency(),
                p.getStatus(), p.getFailureReason(), p.getCreatedOn());
    }
}