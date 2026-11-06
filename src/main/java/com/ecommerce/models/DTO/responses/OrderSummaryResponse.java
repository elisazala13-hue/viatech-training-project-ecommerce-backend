package com.ecommerce.models.DTO.responses;

import com.ecommerce.models.entities.Order;
import com.ecommerce.models.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryResponse(Long id, OrderStatus status, BigDecimal totalAmount, LocalDateTime createdOn) {

    public static OrderSummaryResponse from(Order o) {
        return new OrderSummaryResponse(o.getId(), o.getStatus(), o.getTotalAmount(), o.getCreatedOn());
    }
}