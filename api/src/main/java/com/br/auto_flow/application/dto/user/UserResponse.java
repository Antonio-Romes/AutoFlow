package com.br.auto_flow.application.dto.user;

import com.br.auto_flow.domain.models.enums.Role;
import java.time.Instant;
import java.util.Set;

public record UserResponse(
        long id,
        String name,
        String email,
        Set<Role> roles,
        boolean active,
        Instant createdAt) {
}