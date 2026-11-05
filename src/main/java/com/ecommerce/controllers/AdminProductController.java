package com.ecommerce.controllers;

import com.ecommerce.models.DTO.responses.ProductResponse;
import com.ecommerce.models.enums.ProductStatus;
import com.ecommerce.services.AdminProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService adminProductService;

    @GetMapping
    public Page<ProductResponse> list(
            @RequestParam(required = false) ProductStatus status,
            @PageableDefault(size = 20, sort = "createdOn", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return adminProductService.list(status, pageable);
    }

    @PatchMapping("/{id}/approve")
    public ProductResponse approve(@PathVariable Long id) {
        return adminProductService.approve(id);
    }

    @PatchMapping("/{id}/reject")
    public ProductResponse reject(@PathVariable Long id) {
        return adminProductService.reject(id);
    }
}
