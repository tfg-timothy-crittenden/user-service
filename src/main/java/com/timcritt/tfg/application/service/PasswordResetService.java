package com.timcritt.tfg.application.service;

import com.timcritt.tfg.application.exception.UserNotFoundException;
import com.timcritt.tfg.application.exception.UserNotVerifiedException;
import com.timcritt.tfg.application.port.outbound.EmailSenderPort;
import com.timcritt.tfg.application.port.outbound.PasswordResetTokenRepositoryPort;
import com.timcritt.tfg.application.port.outbound.TokenEncoderPort;
import com.timcritt.tfg.application.port.outbound.UserRepositoryPort;
import com.timcritt.tfg.domain.model.PasswordResetToken;
import com.timcritt.tfg.domain.model.User;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PasswordResetService {

    private final UserRepositoryPort userRepository;
    private final EmailSenderPort emailSender;
    private final TokenEncoderPort tokenHasher;
    private final PasswordResetTokenRepositoryPort passwordResetTokenRepository;

    public PasswordResetService(UserRepositoryPort userRepository, EmailSenderPort emailSenderPort, TokenEncoderPort tokenHasher, PasswordResetTokenRepositoryPort passwordResetTokenRepository) {
        this.userRepository = userRepository;
        this.emailSender = emailSenderPort;
        this.tokenHasher = tokenHasher;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    //request an email with the link
    public void requestPasswordReset(String email) {
        //check user exists
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException(email, null);
        }

        //Invalidate existing password reset tokens
        List<PasswordResetToken> existingTokens = passwordResetTokenRepository.findAllByUserId(userOpt.get().getId());
        existingTokens.forEach(token -> {
            if (token.isValid()) {
                token.setValid(false);
                passwordResetTokenRepository.save(token);
            }

        });

        User user = userOpt.get();

        //Unverified users must verify their account before changing their password
        if(!user.isVerified()) {
            throw new UserNotVerifiedException(email);
        }

        //Generate the token and encode it
        String token = UUID.randomUUID().toString();
        String tokenHash = tokenHasher.encode(token);

        Instant createdAt = Instant.now();
        Instant expiresAt = createdAt.plusSeconds(60 * 60 * 24); //expires after 24 hours


        // create domain token with tokenHash only; valid=true by default
        PasswordResetToken passwordResetToken = new PasswordResetToken(null, user.getId(), tokenHash, createdAt, expiresAt, true );
        passwordResetTokenRepository.save(passwordResetToken);

        //TODO change this link to point at the frontend
        String link = "http://localhost:8082/api/auth/reset-password?token=" + token;

        //Send email with token and link
        emailSender.sendPasswordResetEmail(email, link);


    }


}
