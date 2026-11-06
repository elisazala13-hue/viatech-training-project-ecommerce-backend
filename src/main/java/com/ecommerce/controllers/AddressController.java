package com.ecommerce.controllers;

import com.ecommerce.models.DTO.requests.AddressRequest;
import com.ecommerce.models.DTO.responses.AddressResponse;
import com.ecommerce.security.CustomUserDetails;
import com.ecommerce.services.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public Page<AddressResponse> list(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20, sort = "id",
                    direction = Sort.Direction.DESC)
            Pageable pageable) {

        return addressService.list(userDetails.getUser().getId(), pageable);
    }

    @PostMapping
    public AddressResponse create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AddressRequest request) {
        return addressService.create(userDetails.getUser().getId(), request);
    }

    @PutMapping("/{id}")
    public AddressResponse update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request) {
        return addressService.update(userDetails.getUser().getId(), id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        addressService.delete(userDetails.getUser().getId(), id);
    }
}
