package com.timcritt.tfg.infrastructure.persistence;

import com.timcritt.tfg.application.port.outbound.PasswordResetTokenRepositoryPort;
import com.timcritt.tfg.domain.model.PasswordResetToken;
import com.timcritt.tfg.infrastructure.persistence.spring.PasswordResetTokenJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepositoryPort {

    private final PasswordResetTokenJpaRepository jpaRepository;

    public PasswordResetTokenRepositoryAdapter(final PasswordResetTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<PasswordResetToken> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash).map(PasswordResetTokenRepositoryMapper::toDomain);
    }

    @Override
    public List<PasswordResetToken> findAllByUserId(Long userId) {
        return jpaRepository.findAllByUserId(userId).stream()
                .map(PasswordResetTokenRepositoryMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void save(PasswordResetToken passwordResetToken) {
        // Persist the token entity. Any invalidation of other tokens should be handled in the service layer.
        jpaRepository.save(PasswordResetTokenRepositoryMapper.toEntity(passwordResetToken));
    }

    @Override
    public void delete(PasswordResetToken token) {
        jpaRepository.delete(PasswordResetTokenRepositoryMapper.toEntity(token));
    }

}
