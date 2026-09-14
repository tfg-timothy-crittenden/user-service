package com.timcritt.tfg.domain.exception;

import com.timcritt.tfg.domain.model.Role;

public class UserDoesNotHaveRoleException extends RuntimeException {
    public UserDoesNotHaveRoleException(Role role) {

        super("Role not found on user: " + role);
    }
}
