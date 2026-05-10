// java
package com.timcritt.tfg.infrastructure.persistence;

import com.timcritt.tfg.application.port.outbound.PlatformInvitationRepositoryPort;
import com.timcritt.tfg.domain.model.PlatformInvitation;
import com.timcritt.tfg.domain.model.RoleType;
import com.timcritt.tfg.infrastructure.persistence.jpa.PlatformInvitationJpaEntity;
import com.timcritt.tfg.infrastructure.persistence.spring.PlatformInvitationJpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class PlatformInvitationRepositoryAdapter implements PlatformInvitationRepositoryPort {

    private final PlatformInvitationJpaRepository repository;



    public PlatformInvitationRepositoryAdapter(final PlatformInvitationJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<PlatformInvitation> findPendingByRoleType(RoleType roleType) {
        return this.repository.findPendingByRoleType(roleType).stream().map(PlatformInvitationEntityMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<PlatformInvitation> findByInviteeEmail(String inviteeEmail) {
        return repository.findByInviteeEmail(inviteeEmail).map(PlatformInvitationEntityMapper::toDomain);
    }

    @Override
    public Optional<PlatformInvitation> findByInvitationId(Long invitationId) {
        return repository.findById(invitationId).map(PlatformInvitationEntityMapper::toDomain);
    }


    @Override
    public void save(PlatformInvitation platformInvitation) {
        PlatformInvitationJpaEntity entity = PlatformInvitationEntityMapper.toEntity(platformInvitation);
        repository.save(entity);
    }

    @Override
    public void deleteAllByIds(List<Long> ids) {
        repository.deleteAllByIds(ids);
    }

    @Override
    public List<PlatformInvitation> findAllByIds(List<Long> ids) {
        return repository.findAllById(ids).stream()
                .map(PlatformInvitationEntityMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PlatformInvitation> findByToken(String token) {
        return repository.findByToken(token).map(PlatformInvitationEntityMapper::toDomain);
    }


}
