package com.timcritt.tfg.application.exception;

public class UserNotVerifiedException extends RuntimeException {

    public UserNotVerifiedException(String email)
    {
        super("The account associated with email " + email + " has not been verified via the link sent after signup.");
    }
}
