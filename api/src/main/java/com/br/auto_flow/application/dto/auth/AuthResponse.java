package com.br.auto_flow.application.dto.auth;

public record AuthResponse(
                String accessToken,
                String tokenType,
                long expiresIn,
                String refreshToken) {
}