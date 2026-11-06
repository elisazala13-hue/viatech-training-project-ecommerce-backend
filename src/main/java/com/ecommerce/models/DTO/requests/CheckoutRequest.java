package com.ecommerce.models.DTO.requests;

import jakarta.validation.constraints.NotNull;

public record CheckoutRequest(@NotNull(message = "Address is required") Long addressId) {}