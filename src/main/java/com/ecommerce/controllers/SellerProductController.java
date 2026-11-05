package com.ecommerce.controllers;

import com.ecommerce.models.DTO.responses.ProductResponse;
import com.ecommerce.security.CustomUserDetails;
import com.ecommerce.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sellers/me/products")
@PreAuthorize("hasRole('SELLER')")
@RequiredArgsConstructor
public class SellerProductController {

    private final ProductService productService;

    @GetMapping
    public Page<ProductResponse> list(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PageableDefault(size = 20, sort = "createdOn", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return productService.listMine(principal.getUser().getId(), pageable);
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@AuthenticationPrincipal CustomUserDetails principal,
                                   @PathVariable Long id) {
        return productService.getMine(principal.getUser().getId(), id);
    }
}
