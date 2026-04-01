package com.timcritt.tfg.application.exception;

public class UserNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String usernameOrEmail;




    public UserNotFoundException(Long id, String message) {
        super("User with Id: " + id + " not found. " + message);
        this.usernameOrEmail = null;
        this.id = id;
    }

    public UserNotFoundException(String usernameOrEmail, String message) {
        super("User with identifier: " + usernameOrEmail + " not found");
        this.usernameOrEmail = usernameOrEmail;
        this.id=null;
    }


}
