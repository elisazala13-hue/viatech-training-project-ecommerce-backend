package com.ecommerce.repositories;

import com.ecommerce.models.entities.Role;
import com.ecommerce.models.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(UserRole name);
}
