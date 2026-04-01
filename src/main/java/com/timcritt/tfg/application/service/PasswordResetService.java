package com.timcritt.tfg.application.service;

import com.timcritt.tfg.application.exception.EmailSendFailedException;
import com.timcritt.tfg.application.exception.NewPasswordNotValidException;
import com.timcritt.tfg.application.exception.PasswordResetTokenNotValidException;
import com.timcritt.tfg.application.exception.UserNotFoundException;
import com.timcritt.tfg.application.port.outbound.*;
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
    private final PasswordEncoderPort passwordEncoder;

    public PasswordResetService(UserRepositoryPort userRepository, EmailSenderPort emailSenderPort, TokenEncoderPort tokenHasher, PasswordResetTokenRepositoryPort passwordResetTokenRepository, PasswordEncoderPort passwordEncoder) {
        this.userRepository = userRepository;
        this.emailSender = emailSenderPort;
        this.tokenHasher = tokenHasher;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    //request an email with the link and token to reset the password
    public void requestPasswordReset(String email) {
        //check user exists
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Don't disclose whether the email is registered. Return as if successful.
            return;
        }

        User user = userOpt.get();

        //Unverified users must verify their account before changing their password
        if(!user.isVerified()) {
            // Don't disclose to caller whether the user exists but is unverified. Return as if successful.
            return;
        }

        //Invalidate existing password reset tokens
        List<PasswordResetToken> existingTokens = passwordResetTokenRepository.findAllByUserId(user.getId());
        existingTokens.forEach(token -> {
            if (token.isValid()) {
                token.setValid(false);
                passwordResetTokenRepository.save(token);
            }

        });

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

        //Send email with token and link. Don't leak sending errors to the front — throw a domain-level exception
        try {
            emailSender.sendPasswordResetEmail(email, link);
        } catch (Exception e) {
            // Wrap and rethrow - infrastructure layer or a global handler will decide how to log/translate to HTTP
            throw new EmailSendFailedException("Failed to send password reset email", e);
        }

    }

    public void setNewPassword(String token, String newPassword) {
        //Encode the token
        String encodedToken = tokenHasher.encode(token);

        //Search for the token in the DB
        Optional<PasswordResetToken> passwordResetTokenOpt = passwordResetTokenRepository.findByTokenHash(encodedToken);

        if (passwordResetTokenOpt.isEmpty()) {
            throw new PasswordResetTokenNotValidException("Password reset token not valid");
        }

        PasswordResetToken passwordResetToken = passwordResetTokenOpt.get();

        if(!passwordResetToken.isValid()) {
            throw new PasswordResetTokenNotValidException("Password reset token not valid");
        }

        //Get the user associated with the reset request
        Long userId = passwordResetTokenOpt.get().getUserId();

        Optional<User> requestingUser = userRepository.findById(passwordResetToken.getUserId());
        if (requestingUser.isEmpty()) {
            throw new UserNotFoundException(userId, "");
        }

        //Check if the new password is the same as the old one
        User user = requestingUser.get();
        String currentPasswordHash = user.getPasswordHash();

        if(passwordEncoder.matches(newPassword, currentPasswordHash)) {
            throw new NewPasswordNotValidException("New password must not be the same as the current one");
        }

        //Set the new password and persist
        String newPasswordHash = passwordEncoder.encode(newPassword);
        user.setPasswordHash(newPasswordHash);
        userRepository.save(user);

        //Invalidate the token to prevent it being used again
        passwordResetToken.setValid(false);
        passwordResetTokenRepository.save(passwordResetToken);

    }

}
