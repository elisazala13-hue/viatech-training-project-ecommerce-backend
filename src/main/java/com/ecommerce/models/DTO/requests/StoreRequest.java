package com.ecommerce.models.DTO.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StoreRequest(
        @NotBlank(message = "Store name is required")
        @Size(max = 150, message = "Store name must be up to 150 characters")
        String name,

        @Size(max = 1000, message = "Description must be up to 1000 characters")
        String description
) {}
