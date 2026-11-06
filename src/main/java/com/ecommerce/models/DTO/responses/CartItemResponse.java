package com.ecommerce.models.DTO.responses;

import com.ecommerce.models.entities.CartItem;
import com.ecommerce.models.entities.Product;

import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal,
        int availableStock
) {
    public static CartItemResponse from(CartItem item) {
        Product p = item.getProduct();
       return new CartItemResponse(
                item.getId(), p.getId(), p.getName(), p.getPrice(),
                item.getQuantity(),
                p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())),
                p.getQuantity());
    }
}