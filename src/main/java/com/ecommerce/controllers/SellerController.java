package com.ecommerce.controllers;

import com.ecommerce.models.DTO.SellerApplicationRequest;
import com.ecommerce.models.DTO.SellerProfileResponse;
import com.ecommerce.models.DTO.StoreRequest;
import com.ecommerce.models.DTO.StoreResponse;
import com.ecommerce.security.CustomUserDetails;
import com.ecommerce.services.SellerService;
import com.ecommerce.services.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sellers")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;
    private final StoreService storeService;

    @PostMapping("/apply")
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseStatus(HttpStatus.CREATED)
    public SellerProfileResponse apply(@AuthenticationPrincipal CustomUserDetails principal,
                                       @Valid @RequestBody SellerApplicationRequest request) {
        return sellerService.apply(principal.getUser().getId(), request);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'SELLER')")
    public SellerProfileResponse getMe(@AuthenticationPrincipal CustomUserDetails principal) {
        return sellerService.getMine(principal.getUser().getId());
    }

    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'SELLER')")
    public SellerProfileResponse updateMe(@AuthenticationPrincipal CustomUserDetails principal,
                                          @Valid @RequestBody SellerApplicationRequest request) {
        return sellerService.updateMine(principal.getUser().getId(), request);
    }

    @PostMapping("/me/store")
    @PreAuthorize("hasRole('SELLER')")
    @ResponseStatus(HttpStatus.CREATED)
    public StoreResponse createStore(@AuthenticationPrincipal CustomUserDetails principal,
                                     @Valid @RequestBody StoreRequest request) {
        return storeService.create(principal.getUser().getId(), request);
    }

    @GetMapping("/me/store")
    @PreAuthorize("hasRole('SELLER')")
    public StoreResponse getMyStore(@AuthenticationPrincipal CustomUserDetails principal) {
        return storeService.getMine(principal.getUser().getId());
    }

    @PutMapping("/me/store")
    @PreAuthorize("hasRole('SELLER')")
    public StoreResponse updateMyStore(@AuthenticationPrincipal CustomUserDetails principal,
                                       @Valid @RequestBody StoreRequest request) {
        return storeService.updateMine(principal.getUser().getId(), request);
    }
}
