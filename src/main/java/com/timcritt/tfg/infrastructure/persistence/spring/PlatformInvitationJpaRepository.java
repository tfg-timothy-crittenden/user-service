package com.timcritt.tfg.infrastructure.persistence.spring;

import com.timcritt.tfg.domain.model.PlatformInvitation;
import com.timcritt.tfg.domain.model.RoleType;
import com.timcritt.tfg.infrastructure.persistence.jpa.PlatformInvitationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlatformInvitationJpaRepository extends JpaRepository<PlatformInvitationJpaEntity, Long> {

    Optional<PlatformInvitationJpaEntity> findById(Long invitationId);
    Optional<PlatformInvitationJpaEntity> findByInviteeEmail(String inviteeEmail);
    Optional<PlatformInvitationJpaEntity> findByToken(String Token);

    @Query("SELECT p FROM PlatformInvitationJpaEntity p WHERE p.platformInvitationStatus = 'PENDING' AND p.roleType = :roleType")
    List<PlatformInvitationJpaEntity> findPendingByRoleType(@Param("roleType") RoleType roleType);

    @Modifying
    @Query("DELETE FROM PlatformInvitationJpaEntity p WHERE p.id IN :ids")
    void deleteAllByIds(@Param("ids") List<Long> ids);

}
