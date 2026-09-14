package com.timcritt.tfg.infrastructure.persistence;

import com.timcritt.tfg.domain.model.aggregate.passwordReset.PasswordResetToken;
import com.timcritt.tfg.infrastructure.persistence.jpa.PasswordResetTokenJpaEntity;
import com.timcritt.tfg.infrastructure.persistence.jpa.UserJpaEntity;

public class PasswordResetTokenRepositoryMapper {

    public static PasswordResetToken toDomain(PasswordResetTokenJpaEntity entity) {
        if (entity == null) return null;

        return PasswordResetToken.rehydrate(entity.getId(),
                                        entity.getUser() != null ? entity.getUser().getId() : null,
                                        entity.getTokenHash(),
                                        entity.getCreatedAt(),
                                        entity.getExpiresAt(),
                                        entity.isValid()
        );
    }

    public static PasswordResetTokenJpaEntity toEntity(PasswordResetToken domain) {
        if (domain == null) return null;

        PasswordResetTokenJpaEntity entity = new PasswordResetTokenJpaEntity();
        entity.setId(domain.getId());
        if (domain.getUserId() != null) {
            UserJpaEntity user = new UserJpaEntity();
            user.setId(domain.getUserId());
            entity.setUser(user);
        }
        entity.setTokenHash(domain.getTokenHash());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setValid(domain.isValid()); // NEW
        return entity;
    }
}
