package com.timcritt.tfg.application.port.outbound;

import com.timcritt.tfg.domain.model.PlatformInvitation;
import com.timcritt.tfg.infrastructure.persistence.jpa.PlatformInvitationJpaEntity;

import java.util.Optional;

public interface PlatformInvitationRepositoryPort {

    Optional<PlatformInvitation> findByInviteeEmail(String inviteeEmail);

    Optional<PlatformInvitation> findByInvitationId(Long invitationId);

    void save(PlatformInvitation platformInvitation);


    Optional<PlatformInvitation> findByToken(String token);
}
