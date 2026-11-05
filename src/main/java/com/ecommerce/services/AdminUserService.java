package com.ecommerce.services;

import com.ecommerce.exceptions.ConflictException;
import com.ecommerce.exceptions.ResourceNotFoundException;
import com.ecommerce.models.DTO.responses.UserResponse;
import com.ecommerce.models.entities.User;
import com.ecommerce.models.enums.UserStatus;
import com.ecommerce.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ecommerce.models.enums.UserRole;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<UserResponse> list(UserRole role, UserStatus status, Pageable pageable) {

        Page<User> page;

        if (role != null && status != null) {
            page = userRepository.findByRoleAndStatus(role, status, pageable);
        } else if (role != null) {
            page = userRepository.findByRole(role, pageable);
        } else if (status != null) {
            page = userRepository.findByStatus(status, pageable);
        } else {
            page = userRepository.findAll(pageable);
        }

        return page.map(UserResponse::from);
    }

    @Transactional
    public UserResponse suspend(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() == UserRole.ADMIN) {
            throw new ConflictException("Cannot suspend an admin");
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new ConflictException("User is already suspended");
        }

        user.setStatus(UserStatus.SUSPENDED);

        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse activate(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new ConflictException("User is already active");
        }

        user.setStatus(UserStatus.ACTIVE);

        return UserResponse.from(user);
    }
}
