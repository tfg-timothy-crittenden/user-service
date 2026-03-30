package com.timcritt.tfg.infrastructure.persistence;

import com.timcritt.tfg.application.port.outbound.EmailVerificationTokenRepositoryPort;
import com.timcritt.tfg.domain.model.EmailVerificationToken;
import com.timcritt.tfg.infrastructure.persistence.spring.EmailVerificationTokenJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class EmailVerificationTokenRepositoryAdapter implements EmailVerificationTokenRepositoryPort {

    private final EmailVerificationTokenJpaRepository jpaRepository;

    public EmailVerificationTokenRepositoryAdapter(final EmailVerificationTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<EmailVerificationToken> findById(Long id) {
        return jpaRepository.findById(id).map(EmailVerificationTokenEntityMapper::toDomain);
    }

    @Override
    public Optional<EmailVerificationToken> findByUserEmail(String userEmail) {
        return jpaRepository.findByUserEmail(userEmail).map(EmailVerificationTokenEntityMapper::toDomain);
    }

    @Override
    public Optional<EmailVerificationToken> findByToken(String token) {
        return jpaRepository.findByToken(token).map(EmailVerificationTokenEntityMapper::toDomain);
    }

    @Override
    public void save(EmailVerificationToken emailVerificationToken) {
        jpaRepository.save(EmailVerificationTokenEntityMapper.toEntity(emailVerificationToken));
    }

    @Override
    public void delete(EmailVerificationToken emailVerificationToken) {
        jpaRepository.delete(EmailVerificationTokenEntityMapper.toEntity(emailVerificationToken));
    }
}
