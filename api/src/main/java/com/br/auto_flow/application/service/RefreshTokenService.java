package com.br.auto_flow.application.service;

import com.br.auto_flow.application.dto.auth.AuthResponse;
import com.br.auto_flow.domain.models.user.User;

public interface RefreshTokenService {
    String issue(User user);

    AuthResponse refresh(String rawToken);

    void revoke(String rawToken);
}
