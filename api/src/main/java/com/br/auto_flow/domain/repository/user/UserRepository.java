package com.br.auto_flow.domain.repository.user;

import com.br.auto_flow.domain.models.user.User;
import com.br.auto_flow.domain.valueobject.Email;
import java.util.Optional;

public interface UserRepository {
    boolean existsByEmail(Email email);

    Optional<User> findByEmail(Email email);

    Optional<User> findById(long id);

    User save(User user);
}