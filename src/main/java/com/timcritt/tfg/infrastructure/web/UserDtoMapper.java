package com.timcritt.tfg.infrastructure.web;

import com.timcritt.tfg.domain.model.RoleType;
import com.timcritt.tfg.domain.model.User;
import com.timcritt.tfg.domain.model.Role;
import com.timcritt.tfg.infrastructure.web.dto.UserDto;
import com.timcritt.tfg.infrastructure.web.dto.RoleDto;

import java.util.HashSet;
import java.util.Set;

//Keep passwords out of the DTOs!

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
        dto.setRoles(toRoleDtos(d.getRoles()));
        return dto;
    }

    public static User toDomain(UserDto dto) {
        if (dto == null) return null;

        User user = new User();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setName(dto.getName());
        user.setSurname(dto.getSurname());
        user.setEmail(dto.getEmail());
        user.setRoles(toDomainRoles(dto.getRoles()));
       return user;
    }

    private static Set<RoleDto> toRoleDtos(Set<Role> roles) {
        Set<RoleDto> out = new HashSet<>();
        if (roles == null) return out;

        for (Role r : roles) {
            if (r == null) continue;
            RoleDto dto = new RoleDto();
            dto.setId(r.getId());
            dto.setRoleType(r.getRoleType().name());
            out.add(dto);
        }
        return out;
    }

    private static Set<Role> toDomainRoles(Set<RoleDto> roleDtos) {
        Set<Role> out = new HashSet<>();
        if (roleDtos == null) return out;

        for (RoleDto dto : roleDtos) {
            if (dto == null) continue;
            Role role = new Role();
            role.setId(dto.getId());
            role.setRoleType(RoleType.valueOf(dto.getRoleType()));
            out.add(role);
        }
        return out;
    }
}
