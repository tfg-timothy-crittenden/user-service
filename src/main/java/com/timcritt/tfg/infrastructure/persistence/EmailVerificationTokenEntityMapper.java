package com.timcritt.tfg.infrastructure.persistence;

import com.timcritt.tfg.domain.model.EmailVerificationToken;
import com.timcritt.tfg.infrastructure.persistence.jpa.EmailVerificationTokenJpaEntity;
import com.timcritt.tfg.infrastructure.persistence.jpa.UserJpaEntity;

public class EmailVerificationTokenEntityMapper {

    public static EmailVerificationToken toDomain(EmailVerificationTokenJpaEntity entity) {
        if (entity == null) return null;

        return new EmailVerificationToken(
                entity.getId(),
                entity.getUser() != null ? entity.getUser().getId() : null,
                entity.getUserEmail(),
                entity.getToken(),
                entity.getCreatedAt(),
                entity.getExpiresAt()
        );
    }

    public static EmailVerificationTokenJpaEntity toEntity(EmailVerificationToken domain) {
        if (domain == null) return null;

        EmailVerificationTokenJpaEntity entity = new EmailVerificationTokenJpaEntity();
        entity.setId(domain.getId());
        if (domain.getUserId() != null) {
            UserJpaEntity user = new UserJpaEntity();
            user.setId(domain.getUserId());
            entity.setUser(user);
        }
        entity.setUserEmail(domain.getUserEmail());
        entity.setToken(domain.getToken());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setConfirmedAt(domain.getConfirmedAt());
        entity.setStatus(domain.getStatus());
        return entity;
    }
}
