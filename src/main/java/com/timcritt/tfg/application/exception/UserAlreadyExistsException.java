package com.timcritt.tfg.application.exception;

public class UserAlreadyExistsException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UserAlreadyExistsException(String identifier, String field) {
        super("User with " + field + " '" + identifier + "' already exists.");
    }
}

