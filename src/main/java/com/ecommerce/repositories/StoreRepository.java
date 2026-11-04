package com.ecommerce.repositories;

import com.ecommerce.models.entities.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {
    Optional<Store> findBySellerId(Long sellerId);
}
