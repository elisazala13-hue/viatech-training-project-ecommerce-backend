package com.ecommerce.repositories;

import com.ecommerce.models.entities.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface AddressRepository extends JpaRepository<Address, Long> {
    Page<Address> findByUserId(Long userId, Pageable pageable);
    Optional<Address> findByIdAndUserId(Long id, Long userId);
}
