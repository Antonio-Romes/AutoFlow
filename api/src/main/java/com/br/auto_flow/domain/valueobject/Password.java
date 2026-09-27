package com.br.auto_flow.domain.valueobject;

import com.br.auto_flow.domain.exception.InvalidPasswordException;
import java.util.Objects;

public final class Password {
    private final String value;

    private Password(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidPasswordException("Password must not be blank");
        }
        this.value = value;
    }

    public static Password create(String value) {
        return new Password(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof Password password))
            return false;
        return value.equals(password.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}