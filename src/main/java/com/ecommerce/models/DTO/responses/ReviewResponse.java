package com.ecommerce.models.DTO.responses;

import com.ecommerce.models.entities.Review;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id, Long productId, String userFullName,
        int rating, String comment,  LocalDateTime createdOn
) {
    public static ReviewResponse from(Review r) {
        return new ReviewResponse(
                r.getId(), r.getProduct().getId(), r.getUser().getFullName(),
                r.getRating(), r.getComment(), r.getCreatedOn());
    }
}
