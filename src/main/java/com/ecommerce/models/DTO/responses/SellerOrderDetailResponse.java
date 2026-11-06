package com.ecommerce.models.DTO.responses;


import com.ecommerce.models.entities.Order;
import com.ecommerce.models.entities.SellerOrder;
import com.ecommerce.models.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// order of a customer from the seller's pov
public record SellerOrderDetailResponse(
        Long id,
        Long orderId,
        OrderStatus status,
        BigDecimal subtotal,
        String customerName,
        String customerEmail,
        ShippingAddressResponse shippingAddress,
        List<OrderItemResponse> items,
        LocalDateTime createdOn
) {
    public static SellerOrderDetailResponse from(SellerOrder so) {
        Order o = so.getOrder();
        return new SellerOrderDetailResponse(
                so.getId(), o.getId(), so.getStatus(), so.getSubtotal(),
                o.getCustomer().getFullName(), o.getCustomer().getEmail(),
                ShippingAddressResponse.from(o.getShippingAddress()),
                so.getItems().stream().map(OrderItemResponse::from).toList(),
                so.getCreatedOn());
    }
}