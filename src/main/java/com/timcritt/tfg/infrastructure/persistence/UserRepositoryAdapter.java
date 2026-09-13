package com.timcritt.tfg.infrastructure.persistence;

import com.timcritt.tfg.application.port.outbound.UserRepositoryPort;
import com.timcritt.tfg.domain.model.Role;
import com.timcritt.tfg.domain.model.aggregate.user.User;
import com.timcritt.tfg.infrastructure.persistence.jpa.UserJpaEntity;
import com.timcritt.tfg.infrastructure.persistence.spring.UserJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class UserRepositoryAdapter implements UserRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(UserRepositoryAdapter.class);

    private final UserJpaRepository jpaRepository;


    public UserRepositoryAdapter(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
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
        //TODO: is this still needed after we have refactored and remove RoleType and instead just use Lists of Role enums?
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
                managed.setPasswordHash(user.getPasswordHash().value());
                managed.setVerified(user.isVerified());
                managed.setRoles(user.getRoles());

                UserJpaEntity saved = jpaRepository.save(managed);
                return UserEntityMapper.toDomain(saved);
            }
        }


        // New user path (no existing id / not found): create new entity as before
        UserJpaEntity entity = UserEntityMapper.toEntity(user);
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
    public List<User>  findAllUsersByRole(Role role) {
        List<UserJpaEntity> entities = jpaRepository.findUsersByRole(role);
        return entities.stream().map(UserEntityMapper::toDomain).collect(Collectors.toList());
    }

    private boolean containsUserWithId(Set<UserJpaEntity> set, Long id) {
        if (id == null) return false;
        for (UserJpaEntity u : set) {
            if (u != null && u.getId() != null && u.getId().equals(id)) return true;
        }
        return false;
    }


}
