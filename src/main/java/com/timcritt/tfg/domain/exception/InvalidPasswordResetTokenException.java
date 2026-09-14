package com.timcritt.tfg.domain.exception;

public class InvalidPasswordResetTokenException extends RuntimeException{
    public InvalidPasswordResetTokenException() {
        super("The password reset token is invalid. Request another one");
    }
}
