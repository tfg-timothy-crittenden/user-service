package com.timcritt.tfg.application.port.outbound;

public interface EmailSenderPort {
    void sendVerificationEmail(String to, String verificationLink);
}

