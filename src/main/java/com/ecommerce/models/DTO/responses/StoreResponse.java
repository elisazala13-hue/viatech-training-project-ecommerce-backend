package com.ecommerce.models.DTO.responses;

import com.ecommerce.models.entities.Store;

public record StoreResponse(Long id, Long sellerId, String name, String descriptionl) {

    public static StoreResponse from(Store s) {
        return new StoreResponse(s.getId(), s.getSeller().getId(),
                s.getName(), s.getDescription());
    }
}
