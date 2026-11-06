package com.ecommerce.repositories;

import com.ecommerce.models.entities.SellerOrder;
import com.ecommerce.models.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface SellerOrderRepository extends JpaRepository<SellerOrder, Long> {
    @EntityGraph(attributePaths = {"order", "order.customer", "order.shippingAddress"})
    Page<SellerOrder> findBySellerIdAndStatusNot(Long sellerId, OrderStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"order", "order.customer", "order.shippingAddress"})
    Page<SellerOrder> findBySellerIdAndStatus(Long sellerId, OrderStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"order", "order.customer", "order.shippingAddress"})
    Optional<SellerOrder> findByIdAndSellerId(Long id, Long sellerId);
}
