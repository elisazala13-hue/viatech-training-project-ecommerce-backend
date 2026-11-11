package com.ecommerce.controllers.admin;

import com.ecommerce.models.DTO.responses.OrderResponse;
import com.ecommerce.models.DTO.responses.OrderSummaryResponse;
import com.ecommerce.models.enums.OrderStatus;
import com.ecommerce.services.OrderService;
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
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public Page<OrderSummaryResponse> list(
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 20, sort = "createdOn", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return orderService.listAll(status, pageable);
    }

    @GetMapping("/{id}")
    public OrderResponse getById(@PathVariable Long id) {
        return orderService.getAny(id);
    }
}
