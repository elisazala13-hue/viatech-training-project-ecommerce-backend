package com.ecommerce.controllers;

import com.ecommerce.models.DTO.RejectSellerRequest;
import com.ecommerce.models.DTO.SellerProfileResponse;
import com.ecommerce.models.enums.SellerStatus;
import com.ecommerce.services.AdminSellerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/sellers")
@RequiredArgsConstructor
public class AdminSellerController {

    private final AdminSellerService adminSellerService;

    @GetMapping
    public Page<SellerProfileResponse> list(
            @RequestParam(required = false) SellerStatus status,
            @PageableDefault(size = 20, sort = "createdOn", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return adminSellerService.list(status, pageable);
    }

    @PatchMapping("/{id}/approve")
    public SellerProfileResponse approve(@PathVariable Long id) {
        return adminSellerService.approve(id);
    }

    @PatchMapping("/{id}/reject")
    public SellerProfileResponse reject(@PathVariable Long id,
                                        @Valid @RequestBody RejectSellerRequest request) {
        return adminSellerService.reject(id, request.reason());
    }
}
