package com.timcritt.tfg.application.port.outbound;

import com.timcritt.tfg.domain.model.EmailVerificationToken;

import java.util.Optional;

public interface EmailVerificationTokenRepositoryPort {

    Optional<EmailVerificationToken> findById(Long Id);
    Optional<EmailVerificationToken> findByUserEmail(String token);
    Optional<EmailVerificationToken> findByToken(String token);
    void save(EmailVerificationToken emailVerificationToken);
    void delete(EmailVerificationToken emailVerificationToken);

}
