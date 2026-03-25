package com.timcritt.tfg.infrastructure.persistence;

import com.timcritt.tfg.application.port.outbound.UserRepositoryPort;
import com.timcritt.tfg.domain.model.User;
import com.timcritt.tfg.infrastructure.persistence.jpa.UserJpaEntity;
import com.timcritt.tfg.infrastructure.persistence.spring.UserJpaRepository;
import com.timcritt.tfg.infrastructure.persistence.mapper.UserEntityMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// This class is the adapter that implements the UserRepositoryPort interface and uses Spring Data JPA to interact with the database.

@Repository
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository jpaRepository;

    public UserRepositoryAdapter(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id).map(com.timcritt.tfg.infrastructure.persistence.mapper.UserEntityMapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = com.timcritt.tfg.infrastructure.persistence.mapper.UserEntityMapper.toEntity(user);
        UserJpaEntity saved = jpaRepository.save(entity);
        return UserEntityMapper.toDomain(saved);
    }

    @Override
    public Boolean delete(Long id) {
        jpaRepository.deleteById(id);
        return true;
    }
}
