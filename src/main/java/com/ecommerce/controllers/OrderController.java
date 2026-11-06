package com.ecommerce.controllers;

import com.ecommerce.models.DTO.responses.OrderResponse;
import com.ecommerce.models.DTO.responses.OrderSummaryResponse;
import com.ecommerce.security.CustomUserDetails;
import com.ecommerce.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@PreAuthorize("hasRole('CUSTOMER')")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public Page<OrderSummaryResponse> list(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PageableDefault(size = 10, sort = "createdOn", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return orderService.listMine(principal.getUser().getId(), pageable);
    }

    @GetMapping("/{id}")
    public OrderResponse getById(@AuthenticationPrincipal CustomUserDetails principal,
                                 @PathVariable Long id) {
        return orderService.getMine(principal.getUser().getId(), id);
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@AuthenticationPrincipal CustomUserDetails principal,
                                @PathVariable Long id) {
        return orderService.cancelMine(principal.getUser().getId(), id);
    }
}