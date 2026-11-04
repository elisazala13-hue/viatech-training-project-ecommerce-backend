package com.ecommerce.repositories;

import com.ecommerce.models.entities.SellerOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.print.Pageable;
import java.util.Optional;

public interface SellerOrderRepository extends JpaRepository<SellerOrder, Long> {
    Page<SellerOrder> findBySellerIdOrderByCreatedOnDesc(Long sellerId, Pageable pageable);
    Optional<SellerOrder> findByIdAndSellerId(Long id, Long sellerId);
}
