package com.timcritt.tfg.application.exception;

public class InvitationExpiredException extends RuntimeException {
    public InvitationExpiredException() {
        super("Invitation has expired. Please request a new one.");
    }
}

