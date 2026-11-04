package com.ecommerce.models.DTO;

public record LoginResponse(String token, String tokenType, UserResponse user) {}