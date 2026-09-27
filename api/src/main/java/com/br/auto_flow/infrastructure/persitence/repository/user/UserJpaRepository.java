package com.br.auto_flow.infrastructure.persitence.repository.user;

import com.br.auto_flow.infrastructure.persitence.entity.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByEmail(String email);

    Optional<UserEntity> findByEmail(String email);
}