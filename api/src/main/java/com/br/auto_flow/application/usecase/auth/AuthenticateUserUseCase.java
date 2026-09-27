package com.br.auto_flow.application.usecase.auth;

import com.br.auto_flow.application.dto.auth.AuthResponse;
import com.br.auto_flow.application.dto.auth.LoginRequest;
import com.br.auto_flow.application.service.TokenProvider;
import com.br.auto_flow.application.service.RefreshTokenService;
import com.br.auto_flow.domain.models.user.User;
import com.br.auto_flow.domain.repository.user.UserRepository;
import com.br.auto_flow.domain.valueobject.Email;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticateUserUseCase {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    public AuthenticateUserUseCase(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TokenProvider tokenProvider,
            RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.refreshTokenService = refreshTokenService;
    }

    public AuthResponse execute(LoginRequest request) {
        User user = userRepository.findByEmail(Email.create(request.email()))
                .filter(User::isActive)
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPassword().getValue()))
                .orElseThrow(() -> new BadCredentialsException("E-mail ou senha inválidos"));

        return new AuthResponse(
                tokenProvider.createToken(user),
                "Bearer",
                tokenProvider.getExpirationSeconds(),
                refreshTokenService.issue(user));
    }
}