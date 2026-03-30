package com.timcritt.tfg.infrastructure.persistence.spring;

import com.timcritt.tfg.infrastructure.persistence.jpa.EmailVerificationTokenJPAEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenJpaRespository extends JpaRepository<EmailVerificationTokenJPAEntity, Long> {
    public Optional<EmailVerificationTokenJPAEntity> findByUserEmail(String email);
}

