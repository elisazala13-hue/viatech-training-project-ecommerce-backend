package com.ecommerce.repositories;

import com.ecommerce.models.entities.Product;
import com.ecommerce.models.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByIdAndSellerIdAndStatusNot(Long id, Long sellerId, ProductStatus status);

    @EntityGraph(attributePaths = "category")
    Page<Product> findBySellerIdAndStatusNot(Long sellerId, ProductStatus status, Pageable pageable);

    Optional<Product> findByIdAndStatus(Long id, ProductStatus status);

    @EntityGraph(attributePaths = "category")
    Page<Product> findBySellerIdAndStatus(Long sellerId, ProductStatus status, Pageable pageable);

    @EntityGraph(attributePaths = "category")
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

    @EntityGraph(attributePaths = "category")
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    @EntityGraph(attributePaths = "category")
    Page<Product> findByStatusNot(ProductStatus status, Pageable pageable);

    @Modifying(flushAutomatically = true)
    @Query("UPDATE Product p SET p.quantity = p.quantity - :qty " +
            "WHERE p.id = :id AND p.quantity >= :qty")
    int decreaseStock(@Param("id") Long id, @Param("qty") int qty);

    @Modifying(flushAutomatically = true)
    @Query("UPDATE Product p SET p.quantity = p.quantity + :qty WHERE p.id = :id")
    void increaseStock(@Param("id") Long id, @Param("qty") int qty);
}
