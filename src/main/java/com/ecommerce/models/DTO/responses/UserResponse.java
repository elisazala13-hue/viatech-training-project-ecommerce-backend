package com.ecommerce.models.DTO.responses;

import com.ecommerce.models.entities.User;
import com.ecommerce.models.enums.UserRole;

public record UserResponse(Long id, String email, String fullName, UserRole role) {

    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getFullName(), u.getRole());
    }
}