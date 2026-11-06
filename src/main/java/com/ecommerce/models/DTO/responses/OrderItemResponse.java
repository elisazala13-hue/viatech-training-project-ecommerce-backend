package com.ecommerce.models.DTO.responses;

import com.ecommerce.models.entities.OrderItem;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id, Long productId, String productName,
        BigDecimal unitPrice, int quantity, BigDecimal lineTotal
) {
    public static OrderItemResponse from(OrderItem i) {
        return new OrderItemResponse(
                i.getId(), i.getProduct().getId(), i.getProduct().getName(), i.getUnitPrice(),
                i.getQuantity(), i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())));
    }
}
