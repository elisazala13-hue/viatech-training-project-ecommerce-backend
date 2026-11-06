package com.ecommerce.models.DTO.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequest(

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Invalid phone number")
        String phone,

        @NotBlank(message = "Street is required")
        @Size(max = 150, message = "Street must be up to 150 characters")
        String street,

        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City must be up to 100 characters")
        String city,

        @NotBlank(message = "Country is required")
        @Size(max = 100, message = "Country must be up to 100 characters")
        String country,

        @NotBlank(message = "Postal code is required")
        @Size(max = 20, message = "Postal code must be up to 20 characters")
        String postalCode,

        boolean defaultAddress

) {}