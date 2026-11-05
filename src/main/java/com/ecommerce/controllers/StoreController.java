package com.ecommerce.controllers;

import com.ecommerce.models.DTO.responses.ProductResponse;
import com.ecommerce.models.DTO.responses.StoreResponse;
import com.ecommerce.services.ProductService;
import com.ecommerce.services.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;
    private final ProductService productService;

    @GetMapping("/{id}")
    public StoreResponse getById(@PathVariable Long id) {
        return storeService.getById(id);
    }

    @GetMapping("/{id}/products")
    public Page<ProductResponse> products(
            @PathVariable Long id,
            @PageableDefault(size = 12, sort = "createdOn", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return productService.listByStore(id, pageable);
    }
}
