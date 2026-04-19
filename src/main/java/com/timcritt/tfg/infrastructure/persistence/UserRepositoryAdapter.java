package com.timcritt.tfg.infrastructure.persistence;

import com.timcritt.tfg.application.port.outbound.UserRepositoryPort;
import com.timcritt.tfg.domain.model.Role;
import com.timcritt.tfg.domain.model.RoleType;
import com.timcritt.tfg.domain.model.User;
import com.timcritt.tfg.infrastructure.persistence.jpa.RoleJpaEntity;
import com.timcritt.tfg.infrastructure.persistence.jpa.UserJpaEntity;
import com.timcritt.tfg.infrastructure.persistence.spring.RoleJpaRepository;
import com.timcritt.tfg.infrastructure.persistence.spring.UserJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class UserRepositoryAdapter implements UserRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(UserRepositoryAdapter.class);

    private final UserJpaRepository jpaRepository;
    private final RoleJpaRepository roleJpaRepository;

    public UserRepositoryAdapter(UserJpaRepository jpaRepository, RoleJpaRepository roleJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.roleJpaRepository = roleJpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id).map(UserEntityMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(UserEntityMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(UserEntityMapper::toDomain);
    }

    @Override
    @Transactional
    public User save(User user) {
        // If this is an existing user, update the managed entity to avoid creating detached instances
        if (user.getId() != null) {
            Optional<UserJpaEntity> existing = jpaRepository.findById(user.getId());
            if (existing.isPresent()) {
                UserJpaEntity managed = existing.get();
                // update simple fields
                managed.setUsername(user.getUsername());
                managed.setName(user.getName());
                managed.setSurname(user.getSurname());
                managed.setEmail(user.getEmail());
                managed.setPasswordHash(user.getPasswordHash());
                managed.setVerified(user.isVerified());

                // resolve roles and set on managed entity
                Set<RoleJpaEntity> resolvedRoles = resolveRoles(user.getRoles());
                managed.setUserRoles(resolvedRoles);

                // ensure owning side contains the managed user (compare by id to avoid duplicates)
                for (RoleJpaEntity role : resolvedRoles) {
                    if (!containsUserWithId(role.getUsers(), managed.getId())) {
                        role.getUsers().add(managed);
                    }
                }

                UserJpaEntity saved = jpaRepository.save(managed);
                return UserEntityMapper.toDomain(saved);
            }
        }

        // New user path (no existing id / not found): create new entity as before
        UserJpaEntity entity = UserEntityMapper.toEntity(user);
        Set<RoleJpaEntity> resolvedRoles = resolveRoles(user.getRoles());
        // attach new entity to roles (owning side)
        for (RoleJpaEntity role : resolvedRoles) {
            role.getUsers().add(entity);
        }
        entity.setUserRoles(resolvedRoles);

        UserJpaEntity saved = jpaRepository.save(entity);
        return UserEntityMapper.toDomain(saved);
    }

    @Override
    public Boolean delete(Long id) {
        jpaRepository.deleteById(id);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User>  findAllUsersByRoleType(RoleType roleType) {
        List<UserJpaEntity> entities = jpaRepository.findUsersByRoleType(roleType);
        return entities.stream().map(UserEntityMapper::toDomain).collect(Collectors.toList());
    }

    private boolean containsUserWithId(Set<UserJpaEntity> set, Long id) {
        if (id == null) return false;
        for (UserJpaEntity u : set) {
            if (u != null && u.getId() != null && u.getId().equals(id)) return true;
        }
        return false;
    }

    private Set<RoleJpaEntity> resolveRoles(Set<Role> roles) {
        Set<RoleJpaEntity> resolved = new HashSet<>();
        if (roles == null || roles.isEmpty()) {
            return resolved;
        }

        for (Role role : roles) {
            if (role == null || role.getRoleType() == null) {
                continue;
            }

            RoleType roleType = role.getRoleType();
            RoleJpaEntity managed = roleJpaRepository.findByRoleType(roleType)
                    .orElseThrow(() -> new IllegalStateException("Missing seeded role: " + roleType));
            resolved.add(managed);
        }

        return resolved;
    }
}
