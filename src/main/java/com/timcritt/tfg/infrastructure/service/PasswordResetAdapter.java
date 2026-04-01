package com.timcritt.tfg.infrastructure.service;

import com.timcritt.tfg.application.port.outbound.EmailSenderPort;
import com.timcritt.tfg.application.port.outbound.PasswordResetTokenRepositoryPort;
import com.timcritt.tfg.application.port.outbound.TokenEncoderPort;
import com.timcritt.tfg.application.service.PasswordResetService;
import com.timcritt.tfg.infrastructure.persistence.spring.PasswordResetTokenJpaRepository;

import org.springframework.stereotype.Service;
import com.timcritt.tfg.application.port.outbound.UserRepositoryPort;

@Service
public class PasswordResetAdapter {

    private final PasswordResetService delegate;


    public PasswordResetAdapter(UserRepositoryPort userRepository, EmailSenderPort emailSender, TokenEncoderPort tokenEncoder, PasswordResetTokenRepositoryPort passwordResetTokenJpaRepository) {
        this.delegate = new PasswordResetService(userRepository, emailSender, tokenEncoder, passwordResetTokenJpaRepository);

    }

    public void requestPasswordReset(String email) {
        this.delegate.requestPasswordReset(email);
    }


}
