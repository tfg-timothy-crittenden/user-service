package com.timcritt.tfg.application.exception;

public class NewPasswordNotValidException extends RuntimeException {
    public NewPasswordNotValidException(String message) {
        super(message);
    }
}
