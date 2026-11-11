package com.ecommerce.controllers.admin;

import com.ecommerce.models.DTO.responses.PaymentResponse;
import com.ecommerce.models.enums.PaymentStatus;
import com.ecommerce.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public Page<PaymentResponse> list(
            @RequestParam(required = false) PaymentStatus status,
            @PageableDefault(size = 20, sort = "createdOn", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return paymentService.listAll(status, pageable);
    }

    @GetMapping("/{id}")
    public PaymentResponse getById(@PathVariable Long id) {
        return paymentService.getAny(id);
    }
}