package com.ecommerce.controllers;

import com.ecommerce.models.DTO.requests.CheckoutRequest;
import com.ecommerce.models.DTO.responses.OrderResponse;
import com.ecommerce.security.CustomUserDetails;
import com.ecommerce.services.CheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse checkout(@AuthenticationPrincipal CustomUserDetails principal,
                                  @Valid @RequestBody CheckoutRequest request) {
        return checkoutService.checkout(principal.getUser().getId(), request);
    }
}