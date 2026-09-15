package com.timcritt.tfg.application.exception;

public class InvitationNotFoundException extends RuntimeException {
    public InvitationNotFoundException() {
        super("No platformInvitation found with token");
    }
}
