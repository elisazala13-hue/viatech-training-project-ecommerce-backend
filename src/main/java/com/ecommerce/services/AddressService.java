package com.ecommerce.services;

import com.ecommerce.exceptions.ConflictException;
import com.ecommerce.exceptions.ResourceNotFoundException;
import com.ecommerce.models.DTO.requests.AddressRequest;
import com.ecommerce.models.DTO.responses.AddressResponse;
import com.ecommerce.models.entities.Address;
import com.ecommerce.models.entities.User;
import com.ecommerce.repositories.AddressRepository;
import com.ecommerce.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<AddressResponse> list(Long userId, Pageable pageable) {
        return addressRepository.findByUserId(userId, pageable)
                .map(AddressResponse::from);
    }

    @Transactional
    public AddressResponse create(Long userId, AddressRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Address address = new Address();
        address.setUser(user);
        address.setPhone(request.phone());
        address.setStreet(request.street());
        address.setCity(request.city());
        address.setCountry(request.country());
        address.setPostalCode(request.postalCode());
        address.setDefaultAddress(request.defaultAddress());

        return AddressResponse.from(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse update(Long userId, Long id, AddressRequest request) {

        Address address = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        address.setPhone(request.phone());
        address.setStreet(request.street());
        address.setCity(request.city());
        address.setCountry(request.country());
        address.setPostalCode(request.postalCode());
        address.setDefaultAddress(request.defaultAddress());

        return AddressResponse.from(address);
    }

    @Transactional
    public void delete(Long userId, Long id) {

        Address address = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        try {
            addressRepository.delete(address);
            addressRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Address is used by an order");
        }
    }
}
