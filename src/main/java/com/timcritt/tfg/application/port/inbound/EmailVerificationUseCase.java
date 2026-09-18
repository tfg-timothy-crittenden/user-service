package com.timcritt.tfg.application.port.inbound;

public interface EmailVerificationUseCase {

    void createAndSendToken(Long userId, String userEmail);

    void resendVerificationEmail(String email);

    void confirmToken(String token);
}
