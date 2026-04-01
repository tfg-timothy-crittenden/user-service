package com.timcritt.tfg.infrastructure.persistence.spring;

import com.timcritt.tfg.infrastructure.persistence.jpa.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {

    //Make sure the roles are also queried, as Spring Security needs them in CustomUserDetailsService
    @Query("select u from UserJpaEntity u left join fetch u.roles where u.username = :username")
    Optional<UserJpaEntity> findByUsername(String username);

    // Also fetch roles when looking up by email to avoid lazy-init when mapping outside a transaction
    @Query("select u from UserJpaEntity u left join fetch u.roles where u.email = :email")
    Optional<UserJpaEntity> findByEmail(String email);
}
