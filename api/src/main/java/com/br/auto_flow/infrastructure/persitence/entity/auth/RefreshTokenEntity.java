package com.br.auto_flow.infrastructure.persitence.entity.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
public class RefreshTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(nullable = false)
    private long userId;

    @Column(nullable = false, length = 36)
    private String familyId;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant revokedAt;

    @Column(length = 64)
    private String replacedByHash;

    @Version
    private long version;

    public RefreshTokenEntity(
            String tokenHash,
            long userId,
            String familyId,
            Instant expiresAt) {
        this.tokenHash = tokenHash;
        this.userId = userId;
        this.familyId = familyId;
        this.expiresAt = expiresAt;
    }
}
