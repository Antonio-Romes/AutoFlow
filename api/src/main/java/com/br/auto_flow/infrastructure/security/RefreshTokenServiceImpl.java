package com.br.auto_flow.infrastructure.security;

import com.br.auto_flow.application.dto.auth.AuthResponse;
import com.br.auto_flow.application.exception.InvalidRefreshTokenException;
import com.br.auto_flow.application.service.RefreshTokenService;
import com.br.auto_flow.application.service.TokenProvider;
import com.br.auto_flow.domain.models.user.User;
import com.br.auto_flow.domain.repository.user.UserRepository;
import com.br.auto_flow.infrastructure.persitence.entity.auth.RefreshTokenEntity;
import com.br.auto_flow.infrastructure.persitence.repository.auth.RefreshTokenJpaRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenJpaRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final long expirationMillis;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenServiceImpl(
            RefreshTokenJpaRepository refreshTokenRepository,
            UserRepository userRepository,
            TokenProvider tokenProvider,
            @Value("${security.refresh-token.expiration-ms:2592000000}") long expirationMillis) {
        if (expirationMillis <= 0) {
            throw new IllegalArgumentException("A duração do refresh token deve ser positiva");
        }
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.expirationMillis = expirationMillis;
    }

    @Override
    @Transactional
    public String issue(User user) {
        String rawToken = newRawToken();
        refreshTokenRepository.save(new RefreshTokenEntity(
                hash(rawToken), user.getId(), UUID.randomUUID().toString(),
                Instant.now().plusMillis(expirationMillis)));
        return rawToken;
    }

    @Override
    @Transactional
    public AuthResponse refresh(String rawToken) {
        RefreshTokenEntity current = findByRawToken(rawToken);
        Instant now = Instant.now();
        if (current.getRevokedAt() != null) {
            revokeFamily(current.getFamilyId(), now);
            throw new InvalidRefreshTokenException();
        }
        if (!current.getExpiresAt().isAfter(now)) {
            current.setRevokedAt(now);
            refreshTokenRepository.save(current);
            throw new InvalidRefreshTokenException();
        }

        User user = userRepository.findById(current.getUserId())
                .filter(User::isActive)
                .orElseThrow(() -> {
                    revokeFamily(current.getFamilyId(), now);
                    return new InvalidRefreshTokenException();
                });

        String replacement = newRawToken();
        String replacementHash = hash(replacement);
        current.setRevokedAt(now);
        current.setReplacedByHash(replacementHash);
        refreshTokenRepository.save(current);
        refreshTokenRepository.save(new RefreshTokenEntity(
                replacementHash, user.getId(), current.getFamilyId(), now.plusMillis(expirationMillis)));

        return new AuthResponse(
                tokenProvider.createToken(user),
                "Bearer",
                tokenProvider.getExpirationSeconds(),
                replacement);
    }

    @Override
    @Transactional
    public void revoke(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHash(hash(rawToken)).ifPresent(token -> {
            if (token.getRevokedAt() == null) {
                token.setRevokedAt(Instant.now());
                refreshTokenRepository.save(token);
            }
        });
    }

    private RefreshTokenEntity findByRawToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidRefreshTokenException();
        }
        return refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(InvalidRefreshTokenException::new);
    }

    private void revokeFamily(String familyId, Instant revokedAt) {
        for (RefreshTokenEntity token : refreshTokenRepository.findAllByFamilyId(familyId)) {
            if (token.getRevokedAt() == null) {
                token.setRevokedAt(revokedAt);
                refreshTokenRepository.save(token);
            }
        }
    }

    private String newRawToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 não disponível", exception);
        }
    }
}
