package com.ecommerce.repositories;

import com.ecommerce.models.entities.SellerProfile;
import com.ecommerce.models.enums.SellerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface SellerProfileRepository extends JpaRepository<SellerProfile, Long> {
    Optional<SellerProfile> findByUserId(Long userId);

    Page<SellerProfile> findByStatus(SellerStatus status, Pageable pageable);

    @Override
    Page<SellerProfile> findAll(Pageable pageable);
}