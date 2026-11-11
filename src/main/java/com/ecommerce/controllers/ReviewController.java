package com.ecommerce.controllers;

import com.ecommerce.models.DTO.requests.ReviewRequest;
import com.ecommerce.models.DTO.responses.ReviewResponse;
import com.ecommerce.models.DTO.responses.ReviewSummaryResponse;
import com.ecommerce.security.CustomUserDetails;
import com.ecommerce.services.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/products/{productId}/reviews")
    public Page<ReviewResponse> list(
            @PathVariable Long productId,
            @PageableDefault(size = 10, sort = "createdOn", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return reviewService.listForProduct(productId, pageable);
    }

    @GetMapping("/products/{productId}/reviews/summary")
    public ReviewSummaryResponse summary(@PathVariable Long productId) {
        return reviewService.summary(productId);
    }

    @PostMapping("/products/{productId}/reviews")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'SELLER', 'ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse create(@AuthenticationPrincipal CustomUserDetails principal,
                                 @PathVariable Long productId,
                                 @Valid @RequestBody ReviewRequest request) {
        return reviewService.create(principal.getUser().getId(), productId, request);
    }

    @PutMapping("/reviews/{id}")
    public ReviewResponse update(@AuthenticationPrincipal CustomUserDetails principal,
                                 @PathVariable Long id,
                                 @Valid @RequestBody ReviewRequest request) {
        return reviewService.update(principal.getUser().getId(), id, request);
    }

    @DeleteMapping("/reviews/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) {
        reviewService.delete(principal.getUser().getId(), id);
    }

    @GetMapping("/reviews/mine")
    public Page<ReviewResponse> mine(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PageableDefault(size = 10, sort = "createdOn", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return reviewService.listMine(principal.getUser().getId(), pageable);
    }
}
