package com.ecommerce.models.DTO.responses;

import com.ecommerce.models.entities.Product;
import com.ecommerce.models.entities.ProductImage;
import com.ecommerce.models.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        int quantity,
        ProductStatus status,
        Long categoryId,
        String categoryName,
        Long sellerId,
        List<ProductImageResponse> images,
        LocalDateTime createdOn
) {
    public static ProductResponse from(Product p) {
        List<ProductImageResponse> images = p.getImages().stream()
                .sorted(Comparator.comparing(ProductImage::isPrimaryImage, Comparator.reverseOrder())
                        .thenComparing(ProductImage::getId))
                .map(ProductImageResponse::from)
                .toList();

        return new ProductResponse(
                p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getQuantity(),
                p.getStatus(), p.getCategory().getId(), p.getCategory().getName(),
                p.getSeller().getId(), images, p.getCreatedOn());
    }
}
