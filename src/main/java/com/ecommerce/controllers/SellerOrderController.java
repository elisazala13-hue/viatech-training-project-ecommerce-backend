package com.ecommerce.controllers;

import com.ecommerce.models.DTO.responses.SellerOrderDetailResponse;
import com.ecommerce.models.DTO.requests.UpdateOrderStatusRequest;
import com.ecommerce.models.enums.OrderStatus;
import com.ecommerce.security.CustomUserDetails;
import com.ecommerce.services.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@PreAuthorize("hasRole('SELLER')")
@RequiredArgsConstructor
public class SellerOrderController {

    private final OrderService orderService;

    @GetMapping("/sellers/me/orders")
    public Page<SellerOrderDetailResponse> list(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 20, sort = "createdOn", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return orderService.listForSeller(principal.getUser().getId(), status, pageable);
    }

    @GetMapping("/sellers/me/orders/{id}")
    public SellerOrderDetailResponse getById(@AuthenticationPrincipal CustomUserDetails principal,
                                             @PathVariable Long id) {
        return orderService.getForSeller(principal.getUser().getId(), id);
    }

    @PatchMapping("/seller-orders/{id}/status")
    public SellerOrderDetailResponse updateStatus(@AuthenticationPrincipal CustomUserDetails principal,
                                                  @PathVariable Long id,
                                                  @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateStatus(principal.getUser().getId(), id, request.status());
    }
}
