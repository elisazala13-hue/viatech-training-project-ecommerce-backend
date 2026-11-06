package com.ecommerce.models.DTO.responses;

import com.ecommerce.models.entities.Address;

public record AddressResponse(
        Long id,
        String phone,
        String street,
        String city,
        String country,
        String postalCode,
        boolean defaultAddress
) {
    public static AddressResponse from(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getPhone(),
                address.getStreet(),
                address.getCity(),
                address.getCountry(),
                address.getPostalCode(),
                address.isDefaultAddress()
        );
    }
}
