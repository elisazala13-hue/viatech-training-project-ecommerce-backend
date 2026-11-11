package com.ecommerce.models.DTO.requests;

public record RefundRequest(Long sellerOrderId) {}
// can refund a sellerOrder or the full order so this field is optional