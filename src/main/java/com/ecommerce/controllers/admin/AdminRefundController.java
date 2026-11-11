package com.ecommerce.controllers.admin;

import com.ecommerce.models.DTO.requests.RefundRequest;
import com.ecommerce.models.DTO.responses.RefundResponse;
import com.ecommerce.services.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminRefundController {

    private final RefundService refundService;

    @PostMapping("/orders/{orderId}/refunds")
    @ResponseStatus(HttpStatus.CREATED)
    public RefundResponse refund(@PathVariable Long orderId,
                                 @Valid @RequestBody RefundRequest request) {
        return refundService.refund(orderId, request);
    }

    @GetMapping("/refunds")
    public Page<RefundResponse> list(
            @PageableDefault(size = 20, sort = "createdOn", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return refundService.list(pageable);
    }
}
