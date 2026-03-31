package com.timcritt.tfg.infrastructure.persistence.spring;

import com.timcritt.tfg.infrastructure.persistence.jpa.EmailVerificationTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenJpaRepository extends JpaRepository<EmailVerificationTokenJpaEntity, Long> {
    Optional<EmailVerificationTokenJpaEntity> findByUserEmail(String userEmail);
    Optional<EmailVerificationTokenJpaEntity> findByToken(String token);
}
