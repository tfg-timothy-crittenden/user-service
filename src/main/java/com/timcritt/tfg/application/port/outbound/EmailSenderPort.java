package com.timcritt.tfg.application.port.outbound;

public interface EmailSenderPort {
    void sendVerificationEmail(String to, String verificationLink);
    void sendInvitationEmail(String to, String invitationLink);
    void sendPasswordResetEmail(String to, String passwordResetLink);
}

