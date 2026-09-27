package com.br.auto_flow.application.exception;

import org.springframework.security.core.AuthenticationException;

public class InvalidRefreshTokenException extends AuthenticationException {
    public InvalidRefreshTokenException() {
        super("Refresh token inválido ou expirado");
    }
}
