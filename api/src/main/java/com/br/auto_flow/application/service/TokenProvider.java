package com.br.auto_flow.application.service;

import com.br.auto_flow.domain.models.user.User;

public interface TokenProvider {
    String createToken(User user);

    long getExpirationSeconds();
}