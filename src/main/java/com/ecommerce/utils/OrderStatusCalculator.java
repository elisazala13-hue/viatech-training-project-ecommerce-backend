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
                .min(Comparator.comparingInt(OrderStatusCalculator::progress))
                .orElse(OrderStatus.REFUNDED);
        order.setStatus(status);
    }

    private static int progress(OrderStatus s) {
        return switch (s) {
            case PENDING_PAYMENT -> 0;
            case PAID -> 1;
            case PROCESSING -> 2;
            case SHIPPED -> 3;
            case DELIVERED -> 4;
            case CANCELLED, REFUNDED -> 5;
        };
    }
}
