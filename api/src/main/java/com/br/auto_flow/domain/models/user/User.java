package com.br.auto_flow.domain.models.user;

import com.br.auto_flow.domain.models.enums.Role;
import com.br.auto_flow.domain.valueobject.Email;
import com.br.auto_flow.domain.valueobject.Password;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public final class User {
    private final long id;
    private final Email email;
    private final Password password;
    private final String name;
    private final Set<Role> roles;
    private final boolean active;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Instant lastLoginAt;

    private User(
            long id,
            Email email,
            Password password,
            String name,
            Set<Role> roles,
            boolean active,
            Instant createdAt,
            Instant updatedAt,
            Instant lastLoginAt) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("O nome não pode estar em branco");
        }
        this.id = id;
        this.email = Objects.requireNonNull(email, "O e-mail não pode ser nulo");
        this.password = Objects.requireNonNull(password, "A senha não pode ser nula");
        this.name = name;
        this.roles = Set.copyOf(Objects.requireNonNull(roles, "Os papéis não podem ser nulos"));
        this.active = active;
        this.createdAt = Objects.requireNonNull(createdAt, "A data de criação não pode ser nula");
        this.updatedAt = Objects.requireNonNull(updatedAt, "A data de atualização não pode ser nula");
        this.lastLoginAt = lastLoginAt;
    }

    public static User create(
            long id,
            Email email,
            Password password,
            String name,
            Set<Role> roles,
            boolean active,
            Instant createdAt,
            Instant updatedAt,
            Instant lastLoginAt) {
        return new User(id, email, password, name, roles, active, createdAt, updatedAt, lastLoginAt);
    }

    public long getId() {
        return id;
    }

    public Email getEmail() {
        return email;
    }

    public Password getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }
}