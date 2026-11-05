package com.ecommerce.models.DTO.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SellerApplicationRequest(
        @NotBlank(message = "NIPT is required")
        @Size(max = 50, message = "NIPT must be up to 50 characters")
        String nipt,

        @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Invalid phone number")
        String businessPhone,

        @Size(max = 500, message = "Address must be up to 500 characters")
        String businessAddress
) {}