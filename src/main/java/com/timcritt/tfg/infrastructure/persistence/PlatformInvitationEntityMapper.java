package com.timcritt.tfg.infrastructure.persistence;

import com.timcritt.tfg.domain.model.PlatformInvitation;
import com.timcritt.tfg.infrastructure.persistence.jpa.PlatformInvitationJpaEntity;

public class PlatformInvitationEntityMapper {

    static PlatformInvitation toDomain(PlatformInvitationJpaEntity jpaEntity) {
        PlatformInvitation domain = new PlatformInvitation();
        domain.setId(jpaEntity.getId());
        domain.setCreatedByUserId(jpaEntity.getCreatedByUserId());
        domain.setEmailInvitee(jpaEntity.getInviteeEmail());
        domain.setCreatedAt(jpaEntity.getCreatedAt());
        domain.setexpiresAt(jpaEntity.getExpiresAt());
        domain.setConfirmedAt(jpaEntity.getConfirmedAt());
        domain.setPlatformInvitationStatus(jpaEntity.getPlatformInvitationStatus());
        domain.setToken(jpaEntity.getToken());
        domain.setRoleType(jpaEntity.getRoleType());

        return domain;
    }

    static PlatformInvitationJpaEntity toEntity(PlatformInvitation domain) {
        PlatformInvitationJpaEntity entity = new PlatformInvitationJpaEntity();
        entity.setId(domain.getId());
        entity.setCreatedByUserId(domain.getCreatedByUserId());
        entity.setInviteeEmail(domain.getInviteeEmail());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setConfirmedAt(domain.getConfirmedAt());
        entity.setPlatformInvitationStatus(domain.getPlatformInvitationStatus());
        entity.setToken(domain.getToken());
        entity.setRoleType(domain.getRoleType());

        return entity;

    }

}
