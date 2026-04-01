package com.timcritt.tfg.infrastructure.persistence.spring;

import com.timcritt.tfg.infrastructure.persistence.jpa.PasswordResetTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenJpaRepository extends JpaRepository<PasswordResetTokenJpaEntity, Long> {

    public Optional<PasswordResetTokenJpaEntity> findByTokenHash(String token);
    public List<PasswordResetTokenJpaEntity> findAllByUserId(Long userId);

    // Return all tokens for a user so we can mark them invalid when issuing a new one
    List<PasswordResetTokenJpaEntity> findAllByUserId(Long userId, org.springframework.data.domain.Sort sort);
}
