package com.ecommerce.controllers;

import com.ecommerce.models.DTO.requests.RefundRequest;
import com.ecommerce.models.DTO.responses.RefundResponse;
import com.ecommerce.security.CustomUserDetails;
import com.ecommerce.services.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    @PostMapping("/orders/{orderId}/refunds")
    @ResponseStatus(HttpStatus.CREATED)
    public RefundResponse refund(@AuthenticationPrincipal CustomUserDetails principal,
                                 @PathVariable Long orderId,
                                 @Valid @RequestBody RefundRequest request) {
        return refundService.refundMine(principal.getUser().getId(), orderId, request);
    }

    @GetMapping("/refunds/mine")
    public Page<RefundResponse> mine(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PageableDefault(size = 10, sort = "createdOn", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return refundService.listMine(principal.getUser().getId(), pageable);
    }
}
