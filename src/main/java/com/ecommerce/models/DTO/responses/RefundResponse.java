package com.ecommerce.models.DTO.responses;

import com.ecommerce.models.entities.PaymentRefund;
import com.ecommerce.models.enums.RefundStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RefundResponse(
        Long id, Long orderId, Long sellerOrderId, BigDecimal amount,
        RefundStatus status, LocalDateTime createdOn
) {
    public static RefundResponse from(PaymentRefund r) {
        return new RefundResponse(
                r.getId(),
                r.getPayment().getOrder().getId(),
                r.getSellerOrder() == null ? null : r.getSellerOrder().getId(),
                r.getAmount(), r.getStatus(), r.getCreatedOn());
    }
}
