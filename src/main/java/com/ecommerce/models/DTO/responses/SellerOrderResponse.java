package com.ecommerce.models.DTO.responses;

import com.ecommerce.models.entities.SellerOrder;
import com.ecommerce.models.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

// order data on customer side is grouped by seller orders
public record SellerOrderResponse(
        Long id, Long sellerId,
        OrderStatus status, BigDecimal subtotal, List<OrderItemResponse> items
) {
    public static SellerOrderResponse from(SellerOrder so) {
        return new SellerOrderResponse(
                so.getId(), so.getSeller().getId(),
                so.getStatus(), so.getSubtotal(),
                so.getItems().stream().map(OrderItemResponse::from).toList());
    }
}
