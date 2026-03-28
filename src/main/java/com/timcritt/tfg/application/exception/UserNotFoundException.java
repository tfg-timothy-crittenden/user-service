package com.timcritt.tfg.application.exception;

public class UserNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final Long userId;

    public UserNotFoundException(Long userId) {
        super("User not found with id: " + userId);
        this.userId = userId;
    }

    public UserNotFoundException(Long userId, String message) {
        super(message);
        this.userId = userId;
    }

    public UserNotFoundException(String username) {
        super("User not found with username: " + username);
        this.userId = 0L;
    }

    public Long getTestId() {
        return userId;
    }
}
