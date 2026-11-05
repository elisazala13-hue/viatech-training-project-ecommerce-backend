package com.ecommerce.models.DTO.responses;

import com.ecommerce.models.entities.ProductImage;

public record ProductImageResponse(Long id, String url, boolean primaryImage) {

    public static ProductImageResponse from(ProductImage i) {
        return new ProductImageResponse(i.getId(), i.getUrl(), i.isPrimaryImage());
    }
}
