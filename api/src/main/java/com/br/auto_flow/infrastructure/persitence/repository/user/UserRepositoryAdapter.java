package com.br.auto_flow.infrastructure.persitence.repository.user;

import com.br.auto_flow.domain.models.user.User;
import com.br.auto_flow.domain.repository.user.UserRepository;
import com.br.auto_flow.domain.valueobject.Email;
import com.br.auto_flow.infrastructure.persitence.mapper.user.UserPersistenceMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryAdapter implements UserRepository {
    private final UserJpaRepository jpaRepository;
    private final UserPersistenceMapper mapper;

    public UserRepositoryAdapter(UserJpaRepository jpaRepository, UserPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpaRepository.existsByEmail(email.getValue());
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return jpaRepository.findByEmail(email.getValue()).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findById(long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public User save(User user) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(user)));
    }
}