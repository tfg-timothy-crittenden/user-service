package com.timcritt.tfg.infrastructure.web;

import com.timcritt.tfg.domain.model.Role;
import com.timcritt.tfg.domain.model.RoleType;
import com.timcritt.tfg.domain.model.User;
import com.timcritt.tfg.infrastructure.web.dto.UserDto;

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
        dto.setRoles(toRoleNames(d.getRoles()));
        dto.setVerified(d.isVerified());
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
        if (dto.isVerified() != null) {
            user.setVerified(dto.isVerified());
        }
        return user;
    }

    private static Set<String> toRoleNames(Set<Role> roles) {
        Set<String> out = new HashSet<>();
        if (roles == null) return out;

        for (Role r : roles) {
            if (r == null || r.getRoleType() == null) continue;
            out.add(r.getRoleType().name());
        }
        return out;
    }

    private static Set<Role> toDomainRoles(Set<String> roleNames) {
        Set<Role> out = new HashSet<>();
        if (roleNames == null) return out;

        for (String roleName : roleNames) {
            if (roleName == null || roleName.isBlank()) continue;
            Role role = new Role();
            role.setRoleType(RoleType.valueOf(roleName));
            out.add(role);
        }
        return out;
    }
}
