package com.br.auto_flow.infrastructure.persitence.mapper.user;

import com.br.auto_flow.domain.models.user.User;
import com.br.auto_flow.domain.valueobject.Email;
import com.br.auto_flow.domain.valueobject.Password;
import com.br.auto_flow.infrastructure.persitence.entity.user.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserPersistenceMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", source = "email.value")
    @Mapping(target = "password", source = "password.value")
    UserEntity toEntity(User user);

    default User toDomain(UserEntity entity) {
        return User.create(
                entity.getId(),
                Email.create(entity.getEmail()),
                Password.create(entity.getPassword()),
                entity.getName(),
                entity.getRoles(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getLastLoginAt());
    }
}