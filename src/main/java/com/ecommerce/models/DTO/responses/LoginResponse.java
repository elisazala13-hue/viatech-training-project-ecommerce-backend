package com.ecommerce.models.DTO.responses;

public record LoginResponse(String token, String tokenType, UserResponse user) {}