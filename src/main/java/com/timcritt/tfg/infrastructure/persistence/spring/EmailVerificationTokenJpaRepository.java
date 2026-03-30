package com.timcritt.tfg.infrastructure.persistence.spring;

import com.timcritt.tfg.infrastructure.persistence.jpa.EmailVerificationTokenJPAEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenJpaRepository extends JpaRepository<EmailVerificationTokenJPAEntity, Long> {
    Optional<EmailVerificationTokenJPAEntity> findByUserEmail(String userEmail);
    Optional<EmailVerificationTokenJPAEntity> findByToken(String token);
}
