package com.timcritt.tfg.application.exception;

public class AlreadyHasRoleException extends RuntimeException {
    public AlreadyHasRoleException(String email, String role) {
        super("User with email " + email + " already has role " + role);
    }
}