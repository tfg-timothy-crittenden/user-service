package com.timcritt.tfg.infrastructure.web.dto;

public class RoleDto {

    private Long id;
    private String roleType;

    public RoleDto() {
    }

    public RoleDto(Long id, String roleType) {
        this.id = id;
        this.roleType = roleType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoleType() {
        return roleType;
    }

    public void setRoleType(String roleType) {
        this.roleType = roleType;
    }
}
