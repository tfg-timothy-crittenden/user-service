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

    @Transactional
    @Override
    public User save(User user) {
        if (user.getId() == null) {
            UserJpaEntity entity = UserEntityMapper.toEntity(user);
            UserJpaEntity persisted = jpaRepository.save(entity);
            return UserEntityMapper.toDomain(persisted);
        }

        UserJpaEntity managed = jpaRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Cannot update user " + user.getId() + ": user does not exist"
                ));

        managed.setVersion(user.getVersion());
        managed.setUsername(user.getUsername());
        managed.setName(user.getName());
        managed.setSurname(user.getSurname());
        managed.setEmail(user.getEmail());
        managed.setPasswordHash(user.getPasswordHash().value());
        managed.setVerified(user.isVerified());

        managed.getUserRoles().clear();
        managed.getUserRoles().addAll(user.getRoles());

        return UserEntityMapper.toDomain(managed);
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

}
