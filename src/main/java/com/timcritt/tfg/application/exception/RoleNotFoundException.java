package com.timcritt.tfg.application.exception;

public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(Long userId, String roleType) {
        super("User " + userId + " does not have role " + roleType);
    }
}

