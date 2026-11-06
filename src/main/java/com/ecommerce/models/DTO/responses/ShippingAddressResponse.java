package com.ecommerce.models.DTO.responses;

import com.ecommerce.models.entities.Address;

public record ShippingAddressResponse(
        String phone,
        String street,
        String city,
        String country,
        String postalCode
) {
    public static ShippingAddressResponse from(Address a) {
        return new ShippingAddressResponse(
                a.getPhone(),
                a.getStreet(),
                a.getCity(),
                a.getCountry(),
                a.getPostalCode()
        );
    }
}
