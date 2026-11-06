package com.ecommerce.models.DTO.responses;

import com.ecommerce.models.entities.Order;
import com.ecommerce.models.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        OrderStatus status,
        BigDecimal totalAmount,
        ShippingAddressResponse shippingAddress,
        List<SellerOrderResponse> sellerOrders,
        LocalDateTime createdOn
) {
    public static OrderResponse from(Order o) {
        return new OrderResponse(
                o.getId(), o.getStatus(), o.getTotalAmount(),
                ShippingAddressResponse.from(o.getShippingAddress()),
                o.getSellerOrders().stream().map(SellerOrderResponse::from).toList(),
                o.getCreatedOn());
    }
}