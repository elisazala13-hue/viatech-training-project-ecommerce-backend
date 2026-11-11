package com.ecommerce.repositories;

import com.ecommerce.models.entities.OrderItem;
import com.ecommerce.models.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT COUNT(oi) > 0 FROM OrderItem oi " +
            "WHERE oi.product.id = :productId " +
            "AND oi.sellerOrder.order.customer.id = :userId " +
            "AND oi.sellerOrder.status = :status")
    boolean hasPurchased(@Param("productId") Long productId,
                         @Param("userId") Long userId,
                         @Param("status") OrderStatus status);
}
