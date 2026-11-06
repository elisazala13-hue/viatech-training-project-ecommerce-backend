package com.ecommerce.models.DTO.requests;

import com.ecommerce.models.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(@NotNull(message = "Status is required") OrderStatus status) {}