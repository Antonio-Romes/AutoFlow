package com.br.auto_flow.application.mapper.user;

import com.br.auto_flow.application.dto.user.UserResponse;
import com.br.auto_flow.domain.models.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "email", source = "email.value")
    UserResponse toResponse(User user);
}