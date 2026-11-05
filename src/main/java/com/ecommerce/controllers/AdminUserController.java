package com.ecommerce.controllers;

import com.ecommerce.models.DTO.responses.UserResponse;
import com.ecommerce.models.enums.UserRole;
import com.ecommerce.models.enums.UserStatus;
import com.ecommerce.services.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public Page<UserResponse> list(
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) UserStatus status,
            @PageableDefault(size = 20, sort = "createdOn",
                    direction = Sort.Direction.DESC)
            Pageable pageable) {

        return adminUserService.list(role, status, pageable);
    }

    @PatchMapping("/{id}/suspend")
    public UserResponse suspend(@PathVariable Long id) {
        return adminUserService.suspend(id);
    }

    @PatchMapping("/{id}/activate")
    public UserResponse activate(@PathVariable Long id) {
        return adminUserService.activate(id);
    }
}
