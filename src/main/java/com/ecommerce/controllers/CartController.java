package com.ecommerce.controllers;

import com.ecommerce.models.DTO.requests.AddToCartRequest;
import com.ecommerce.models.DTO.responses.CartResponse;
import com.ecommerce.models.DTO.requests.UpdateCartItemRequest;
import com.ecommerce.security.CustomUserDetails;
import com.ecommerce.services.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartResponse getCart(@AuthenticationPrincipal CustomUserDetails principal) {
        return cartService.getCart(principal.getUser().getId());
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartResponse addItem(@AuthenticationPrincipal CustomUserDetails principal,
                                @Valid @RequestBody AddToCartRequest request) {
        return cartService.addItem(principal.getUser().getId(), request);
    }

    @PutMapping("/items/{id}")
    public CartResponse updateItem(@AuthenticationPrincipal CustomUserDetails principal,
                                   @PathVariable Long id,
                                   @Valid @RequestBody UpdateCartItemRequest request) {
        return cartService.updateItem(principal.getUser().getId(), id, request);
    }

    @DeleteMapping("/items/{id}")
    public CartResponse removeItem(@AuthenticationPrincipal CustomUserDetails principal,
                                   @PathVariable Long id) {
        return cartService.removeItem(principal.getUser().getId(), id);
    }

    @DeleteMapping
    public CartResponse clearCart(@AuthenticationPrincipal CustomUserDetails principal) {
        return cartService.clearCart(principal.getUser().getId());
    }
}