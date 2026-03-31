package com.timcritt.tfg.infrastructure.service;

import com.timcritt.tfg.application.port.outbound.EmailSenderPort;
import com.timcritt.tfg.application.port.outbound.EmailVerificationTokenRepositoryPort;
import com.timcritt.tfg.application.port.outbound.UserRepositoryPort;
import com.timcritt.tfg.application.service.EmailVerificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailVerificationAdapter {

    private final EmailVerificationService delegate;

    public EmailVerificationAdapter(EmailVerificationTokenRepositoryPort tokenRepo, EmailSenderPort sender, UserRepositoryPort userRepo) {
        this.delegate = new EmailVerificationService(tokenRepo, sender, userRepo);
    }

    @Transactional
    public void createAndSendToken(Long userId, String userEmail) {
        delegate.createAndSendToken(userId, userEmail);
    }

    @Transactional
    public void confirmToken(String token) {
        delegate.confirmToken(token);
    }
}

