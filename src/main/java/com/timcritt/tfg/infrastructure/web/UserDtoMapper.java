package com.timcritt.tfg.infrastructure.web;

import com.timcritt.tfg.domain.model.User;
import com.timcritt.tfg.infrastructure.web.dto.UserDto;

public final class UserDtoMapper {
    private UserDtoMapper() {}

    public static UserDto toDto(User d) {
        if (d == null) return null;
        UserDto dto = new UserDto();
        dto.setId(d.getId());
        dto.setUsername(d.getUsername());
        dto.setName(d.getName());
        dto.setSurname(d.getSurname());
        dto.setEmail(d.getEmail());
        return dto;
    }

    public static User toDomain(UserDto dto) {
        if (dto == null) return null;
        return new User(
                dto.getId(),
                dto.getUsername(),
                dto.getName(),
                dto.getSurname(),
                dto.getEmail()
        );
    }
}
