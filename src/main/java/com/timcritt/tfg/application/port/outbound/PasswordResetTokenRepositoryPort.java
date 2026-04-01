package com.timcritt.tfg.application.port.outbound;

import com.timcritt.tfg.domain.model.PasswordResetToken;

import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepositoryPort {

    Optional<PasswordResetToken> findByTokenHash(String token);
    void save(PasswordResetToken token);
    void delete(PasswordResetToken token);
    List<PasswordResetToken> findAllByUserId(Long user_id);

}
