package com.ecommerce.repositories;

import com.ecommerce.models.entities.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.print.Pageable;
import java.util.Optional;


public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByCustomerIdOrderByCreatedOnDesc(Long customerId, Pageable pageable); // GET /orders
    Optional<Order> findByIdAndCustomerId(Long id, Long customerId);
}