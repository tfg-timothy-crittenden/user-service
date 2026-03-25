package com.timcritt.tfg.domain.model;

import com.timcritt.tfg.domain.model.RoleType;

public class Role {

    private Long id;
    private RoleType roleType;

    public Role() {
    }

    public Role(Long id, RoleType roleType) {
        this.id = id;
        this.roleType = roleType;
    }

    public Long getId() {
        return id;
    }

    public Role setId(Long id) {
        this.id = id;
        return this;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public Role setRoleType(RoleType roleType) {
        this.roleType = roleType;
        return this;
    }
}
