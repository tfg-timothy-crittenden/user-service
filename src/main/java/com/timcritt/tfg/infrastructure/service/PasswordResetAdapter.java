package com.timcritt.tfg.infrastructure.service;

import com.timcritt.tfg.application.port.outbound.*;
import com.timcritt.tfg.application.service.PasswordResetService;
import com.timcritt.tfg.infrastructure.persistence.spring.PasswordResetTokenJpaRepository;

import io.jsonwebtoken.security.Password;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetAdapter {

    private final PasswordResetService delegate;


    public PasswordResetAdapter(UserRepositoryPort userRepository, EmailSenderPort emailSender, TokenEncoderPort tokenEncoder, PasswordResetTokenRepositoryPort passwordResetTokenJpaRepository, PasswordEncoderPort passwordEncoder) {
        this.delegate = new PasswordResetService(userRepository, emailSender, tokenEncoder, passwordResetTokenJpaRepository, passwordEncoder);

    }

    public void requestPasswordReset(String email) {
        this.delegate.requestPasswordReset(email);
    }

    public void setNewPassword(String token, String password) {
        this.delegate.setNewPassword(token, password);
    }


}
