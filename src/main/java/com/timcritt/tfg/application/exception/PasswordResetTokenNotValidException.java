package com.timcritt.tfg.application.exception;

public class PasswordResetTokenNotValidException extends RuntimeException {
    public PasswordResetTokenNotValidException(String message) {
        super(message);
    }
}

