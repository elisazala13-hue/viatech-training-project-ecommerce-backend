package com.ecommerce.repositories;

import com.ecommerce.models.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
public interface ReviewRepository extends JpaRepository<Review, Long> {

    interface RatingSummary {
        Double getAverage();
        Long getCount();
    }

    @EntityGraph(attributePaths = "user")
    Page<Review> findByProductId(Long productId, Pageable pageable);

    @EntityGraph(attributePaths = "user")
    Page<Review> findByUserId(Long userId, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "user")
    Page<Review> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "user")
    Optional<Review> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    @Query("""
        SELECT AVG(r.rating) AS average, COUNT(r) AS count
        FROM Review r
        WHERE r.product.id = :productId
    """)
    RatingSummary summarize(@Param("productId") Long productId);
}