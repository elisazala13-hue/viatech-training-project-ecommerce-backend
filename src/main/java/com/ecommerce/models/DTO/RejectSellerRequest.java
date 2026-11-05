package com.ecommerce.models.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectSellerRequest(
        @NotBlank(message = "Reason is required")
        @Size(max = 500, message = "Reason must be up to 500 characters")
        String reason
) {}
