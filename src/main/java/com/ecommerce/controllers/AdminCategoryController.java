package com.ecommerce.controllers;

import com.ecommerce.models.DTO.requests.CategoryRequest;
import com.ecommerce.models.DTO.responses.CategoryResponse;
import com.ecommerce.services.AdminCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    @GetMapping
    public Page<CategoryResponse> list(
            @PageableDefault(size = 20, sort = "name",
                    direction = Sort.Direction.ASC)
            Pageable pageable) {
        return adminCategoryService.list(pageable);
    }

    @PostMapping
    public CategoryResponse create(
            @Valid @RequestBody CategoryRequest request) {
        return adminCategoryService.create(request);
    }

    @PutMapping("/{id}")
    public CategoryResponse update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        return adminCategoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        adminCategoryService.delete(id);
    }
}
