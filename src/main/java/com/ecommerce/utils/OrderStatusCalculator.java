package com.ecommerce.utils;

import com.ecommerce.models.entities.Order;
import com.ecommerce.models.entities.SellerOrder;
import com.ecommerce.models.enums.OrderStatus;

import java.util.Comparator;

public final class OrderStatusCalculator {

    private OrderStatusCalculator() {}

    // sets overall order status based on the slowest seller order
    public static void recalculate(Order order) {
        OrderStatus status = order.getSellerOrders().stream()
                .map(SellerOrder::getStatus)
                .filter(s -> s != OrderStatus.REFUNDED && s != OrderStatus.CANCELLED)
                .min(Comparator.naturalOrder())
                .orElse(OrderStatus.REFUNDED);
        order.setStatus(status);
    }
}
