// java
package com.timcritt.tfg.application.exception;

public class InvitationNotFoundException extends RuntimeException {
    public InvitationNotFoundException(String token) {
        super("No platformInvitation found with token " + token);
    }
}
