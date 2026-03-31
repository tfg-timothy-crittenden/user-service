package com.timcritt.tfg.application.exception;

public class ActiveInvitationExistsException extends RuntimeException {
    public ActiveInvitationExistsException(String email) {
        super("An active invitation already exists for " + email);
    }
}
