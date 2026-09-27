package com.br.auto_flow.infrastructure.security;

import com.br.auto_flow.application.service.TokenProvider;
import com.br.auto_flow.domain.models.enums.Role;
import com.br.auto_flow.domain.models.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService implements TokenProvider {
    private final PrivateKey signingKey;
    private final PublicKey verificationKey;
    private final long expirationMillis;
    private final String issuer;

    public JwtTokenService(
            @Value("${security.jwt.private-key-path:keys/jwt-private.pem}") String privateKeyPath,
            @Value("${security.jwt.public-key-path:keys/jwt-public.pem}") String publicKeyPath,
            @Value("${security.jwt.expiration-ms:3600000}") long expirationMillis,
            @Value("${security.jwt.issuer:auto_flow}") String issuer) {
        if (expirationMillis <= 0) {
            throw new IllegalArgumentException("A duração do token deve ser positiva");
        }
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException("O emissor do token não pode estar em branco");
        }
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            this.signingKey = keyFactory
                    .generatePrivate(new PKCS8EncodedKeySpec(readPem(privateKeyPath, "PRIVATE KEY")));
            this.verificationKey = keyFactory
                    .generatePublic(new X509EncodedKeySpec(readPem(publicKeyPath, "PUBLIC KEY")));
        } catch (IOException | GeneralSecurityException exception) {
            throw new IllegalStateException("Não foi possível carregar as chaves RSA do JWT", exception);
        }
        this.expirationMillis = expirationMillis;
        this.issuer = issuer;
    }

    @Override
    public String createToken(User user) {
        if (user.getId() <= 0) {
            throw new IllegalArgumentException("O usuário precisa ter um ID válido para emitir o token");
        }
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(Long.toString(user.getId()))
                .issuer(issuer)
                .id(Long.toString(user.getId()))
                .claim("roles", user.getRoles().stream().map(Enum::name).toList())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMillis)))
                .signWith(signingKey, Jwts.SIG.RS256)
                .compact();
    }

    public Jws<Claims> parseToken(String token) {
        Jws<Claims> parsedToken = Jwts.parser()
                .verifyWith(verificationKey)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token);
        validateClaims(parsedToken.getPayload());
        return parsedToken;
    }

    private static void validateClaims(Claims claims) {
        try {
            if (claims.getSubject() == null || Long.parseLong(claims.getSubject()) <= 0) {
                throw new MalformedJwtException("O subject do token deve ser um ID de usuário válido");
            }
        } catch (NumberFormatException exception) {
            throw new MalformedJwtException("O subject do token deve ser um ID de usuário válido", exception);
        }

        Date issuedAt = claims.getIssuedAt();
        Date expiration = claims.getExpiration();
        if (issuedAt == null || expiration == null || !expiration.after(issuedAt)) {
            throw new MalformedJwtException("As datas de emissão e expiração do token são inválidas");
        }
        if (issuedAt.after(new Date())) {
            throw new MalformedJwtException("O token ainda não foi emitido");
        }

        try {
            if (claims.getId() == null || Long.parseLong(claims.getId()) <= 0) {
                throw new MalformedJwtException("O identificador do token é inválido");
            }
        } catch (NumberFormatException exception) {
            throw new MalformedJwtException("O identificador do token é inválido", exception);
        }

        Object roles = claims.get("roles");
        if (!(roles instanceof List<?> roleList)
                || roleList.stream().anyMatch(role -> !(role instanceof String value) || !isKnownRole(value))) {
            throw new MalformedJwtException("A claim roles do token é inválida");
        }
    }

    private static boolean isKnownRole(String role) {
        try {
            Role.valueOf(role);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static byte[] readPem(String path, String label) throws IOException {
        String pem = Files.readString(Path.of(path))
                .replace("-----BEGIN " + label + "-----", "")
                .replace("-----END " + label + "-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(pem);
    }

    @Override
    public long getExpirationSeconds() {
        return expirationMillis / 1000;
    }
}