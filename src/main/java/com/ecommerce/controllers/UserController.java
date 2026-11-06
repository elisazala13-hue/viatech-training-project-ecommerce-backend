package com.ecommerce.controllers;

import com.ecommerce.models.DTO.requests.UpdateProfileRequest;
import com.ecommerce.models.DTO.responses.UserResponse;
import com.ecommerce.security.CustomUserDetails;
import com.ecommerce.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasAnyRole('CUSTOMER', 'SELLER', 'ADMIN')")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal CustomUserDetails principal) {
        return userService.getProfile(principal.getUser().getId());
    }

    @PutMapping("/me")
    public UserResponse update(@AuthenticationPrincipal CustomUserDetails principal,
                               @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(principal.getUser().getId(), request);
    }
}
