package com.ecommerce.models.DTO;

import com.ecommerce.models.entities.User;

public record UserResponse(Long id, String email, String fullName, String role) {

    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getFullName(),
               u.getRole().getName().name());
    }
}