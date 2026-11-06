package com.ecommerce.models.DTO.requests;

import jakarta.validation.constraints.NotNull;

public record CreatePaymentIntentRequest(@NotNull(message = "Order is required") Long orderId) {}
