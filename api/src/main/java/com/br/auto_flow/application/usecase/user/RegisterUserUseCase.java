package com.br.auto_flow.application.usecase.user;

import com.br.auto_flow.application.dto.user.RegisterUserRequest;
import com.br.auto_flow.domain.exception.DomainException;
import com.br.auto_flow.domain.models.enums.Role;
import com.br.auto_flow.domain.models.user.User;
import com.br.auto_flow.domain.repository.user.UserRepository;
import com.br.auto_flow.domain.valueobject.Email;
import com.br.auto_flow.domain.valueobject.Password;
import java.time.Instant;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegisterUserUseCase {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User execute(RegisterUserRequest request) {
        Email email = Email.create(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyRegisteredException();
        }

        Instant now = Instant.now();
        User user = User.create(
                0,
                email,
                Password.create(passwordEncoder.encode(request.password())),
                request.name(),
                Set.of(Role.USER),
                true,
                now,
                now,
                null);
        return userRepository.save(user);
    }

    public static class UserAlreadyRegisteredException extends DomainException {
        public UserAlreadyRegisteredException() {
            super("Este e-mail já está cadastrado");
        }
    }
}