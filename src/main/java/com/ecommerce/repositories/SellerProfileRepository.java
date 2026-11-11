package com.ecommerce.repositories;

import com.ecommerce.models.entities.SellerProfile;
import com.ecommerce.models.enums.SellerStatus;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

@NullMarked
public interface SellerProfileRepository extends JpaRepository<SellerProfile, Long> {
    Optional<SellerProfile> findByUserId(Long userId);

    Page<SellerProfile> findByStatus(SellerStatus status, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "user")
    Page<SellerProfile> findAll(Pageable pageable);
}