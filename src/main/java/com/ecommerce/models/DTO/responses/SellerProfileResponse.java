package com.ecommerce.models.DTO.responses;

import com.ecommerce.models.entities.SellerProfile;
import com.ecommerce.models.enums.SellerStatus;

import java.time.LocalDateTime;

public record SellerProfileResponse(
        Long id,
        UserResponse user,
        String nipt,
        String businessPhone,
        String businessAddress,
        SellerStatus status,
        String rejectionReason,
        LocalDateTime createdOn
) {
    public static SellerProfileResponse from(SellerProfile p) {
        return new SellerProfileResponse(
                p.getId(),
                UserResponse.from(p.getUser()),
                p.getNipt(),
                p.getBusinessPhone(),
                p.getBusinessAddress(),
                p.getStatus(),
                p.getRejectionReason(),
                p.getCreatedOn()
        );
    }
}