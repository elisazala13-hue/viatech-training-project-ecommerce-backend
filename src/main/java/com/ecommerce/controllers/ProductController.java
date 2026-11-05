package com.ecommerce.controllers;

import com.ecommerce.models.DTO.responses.ProductImageResponse;
import com.ecommerce.models.DTO.requests.ProductRequest;
import com.ecommerce.models.DTO.responses.ProductResponse;
import com.ecommerce.security.CustomUserDetails;
import com.ecommerce.services.ProductImageService;
import com.ecommerce.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductImageService productImageService;

    @GetMapping
    public Page<ProductResponse> search(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 12, sort = "createdOn", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return productService.search(categoryId, q, pageable);
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return productService.getPublic(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@AuthenticationPrincipal CustomUserDetails principal,
                                  @Valid @RequestBody ProductRequest request) {
        return productService.create(principal.getUser().getId(), request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ProductResponse update(@AuthenticationPrincipal CustomUserDetails principal,
                                  @PathVariable Long id,
                                  @Valid @RequestBody ProductRequest request) {
        return productService.update(principal.getUser().getId(), id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal CustomUserDetails principal,
                       @PathVariable Long id) {
        productService.delete(principal.getUser().getId(), id);
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SELLER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductImageResponse uploadImage(@AuthenticationPrincipal CustomUserDetails principal,
                                            @PathVariable Long id,
                                            @RequestParam("file") MultipartFile file) {
        return productImageService.upload(principal.getUser().getId(), id, file);
    }

    @DeleteMapping("/{id}/images/{imageId}")
    @PreAuthorize("hasRole('SELLER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteImage(@AuthenticationPrincipal CustomUserDetails principal,
                            @PathVariable Long id,
                            @PathVariable Long imageId) {
        productImageService.delete(principal.getUser().getId(), id, imageId);
    }
}