package com.br.auto_flow.infrastructure.persitence.repository.auth;

import com.br.auto_flow.infrastructure.persitence.entity.auth.RefreshTokenEntity;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    int deleteByExpiresAtBefore(Instant now);

    Iterable<RefreshTokenEntity> findAllByFamilyId(String familyId);
}
