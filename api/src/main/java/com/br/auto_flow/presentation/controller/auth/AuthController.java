package com.br.auto_flow.presentation.controller.auth;

import com.br.auto_flow.application.dto.auth.AuthResponse;
import com.br.auto_flow.application.dto.auth.LoginRequest;
import com.br.auto_flow.application.dto.auth.RefreshTokenRequest;
import com.br.auto_flow.application.service.RefreshTokenService;
import com.br.auto_flow.application.usecase.auth.AuthenticateUserUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final RefreshTokenService refreshTokenService;

    public AuthController(
            AuthenticateUserUseCase authenticateUserUseCase,
            RefreshTokenService refreshTokenService) {
        this.authenticateUserUseCase = authenticateUserUseCase;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticateUserUseCase.execute(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(refreshTokenService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        refreshTokenService.revoke(request.refreshToken());
        return ResponseEntity.noContent().build();
    }
}