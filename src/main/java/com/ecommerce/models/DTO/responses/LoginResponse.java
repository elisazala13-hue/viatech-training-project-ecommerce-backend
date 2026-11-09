package com.ecommerce.models.DTO.responses;

public record LoginResponse(String accessToken, String refreshToken,
                            String tokenType, UserResponse user) {}