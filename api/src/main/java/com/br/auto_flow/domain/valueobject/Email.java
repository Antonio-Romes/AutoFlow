package com.br.auto_flow.domain.valueobject;

import com.br.auto_flow.domain.exception.InvalidEmailException;
import java.util.Objects;
import java.util.regex.Pattern;

public final class Email {
    private static final Pattern FORMAT = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final String value;

    private Email(String value) {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw new InvalidEmailException("Invalid email address");
        }
        this.value = value;
    }

    public static Email create(String value) {
        return new Email(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof Email email))
            return false;
        return value.equals(email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}