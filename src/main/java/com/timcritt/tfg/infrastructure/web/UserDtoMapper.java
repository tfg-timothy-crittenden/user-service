package com.timcritt.tfg.infrastructure.web;

import com.timcritt.tfg.domain.model.Role;
import com.timcritt.tfg.domain.model.RoleType;
import com.timcritt.tfg.domain.model.aggregate.user.User;
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

    private static Set<String> toRoleNames(Set<Role> roles) {
        Set<String> out = new HashSet<>();
        if (roles == null) return out;

        for (Role r : roles) {
            if (r == null || r.getRoleType() == null) continue;
            out.add(r.getRoleType().name());
        }
        return out;
    }

}
