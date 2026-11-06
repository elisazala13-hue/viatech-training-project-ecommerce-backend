package com.ecommerce.models.DTO.responses;

import com.ecommerce.models.entities.Payment;

import java.math.BigDecimal;

// stripe intent data sent to frontend stripe.js to proceed with payment handling
public record PaymentIntentResponse(Long paymentId, String clientSecret, BigDecimal amount, String currency) {

    public static PaymentIntentResponse of(Payment p, String clientSecret) {
        return new PaymentIntentResponse(p.getId(), clientSecret, p.getAmount(), p.getCurrency());
    }
}