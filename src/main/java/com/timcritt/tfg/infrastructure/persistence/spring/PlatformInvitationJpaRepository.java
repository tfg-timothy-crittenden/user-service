package com.timcritt.tfg.infrastructure.persistence.spring;

import com.timcritt.tfg.domain.model.PlatformInvitation;
import com.timcritt.tfg.infrastructure.persistence.jpa.PlatformInvitationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlatformInvitationJpaRepository extends JpaRepository<PlatformInvitationJpaEntity, Long> {

    Optional<PlatformInvitationJpaEntity> findById(Long invitationId);
    Optional<PlatformInvitationJpaEntity> findByInviteeEmail(String inviteeEmail);
    Optional<PlatformInvitationJpaEntity> findByToken(String Token);
}
