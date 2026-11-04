package com.ecommerce.repositories;

import com.ecommerce.models.entities.Product;
import com.ecommerce.models.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findBySellerId(Long sellerId, Pageable pageable);

    @Query("""
           SELECT p FROM Product p
           WHERE p.status = :status
             AND (:categoryId IS NULL OR p.category.id = :categoryId)
             AND (:q IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')))
           """)
    Page<Product> search(@Param("status") ProductStatus status,
                         @Param("categoryId") Long categoryId,
                         @Param("q") String q,
                         Pageable pageable);
}
