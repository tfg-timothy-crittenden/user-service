package com.timcritt.tfg.application.exception;

public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(Long userId, String role) {
        super("User " + userId + " does not have role " + role);
    }
}

