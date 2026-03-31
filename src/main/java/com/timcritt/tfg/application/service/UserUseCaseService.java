package com.timcritt.tfg.application.service;

import com.timcritt.tfg.application.exception.UserNotFoundException;
import com.timcritt.tfg.application.port.inbound.UserUseCase;
import com.timcritt.tfg.application.port.outbound.UserRepositoryPort;
import com.timcritt.tfg.domain.model.RoleType;
import com.timcritt.tfg.domain.model.User;

import java.util.Optional;

// This class contains business logic for handling User operations.
public class UserUseCaseService implements UserUseCase {

    private final UserRepositoryPort repository;

    public UserUseCaseService(UserRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public User getUserById(Long id) {
        return repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public User getUserByUsername(String username) {
        // Try username first, then fall back to email lookup for convenience
        return repository.findByUsername(username)
                .or(() -> repository.findByEmail(username))
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    @Override
    public Optional<User> findByIdentifier(String usernameOrEmail) {
        return repository.findByUsername(usernameOrEmail)
                .or(() -> repository.findByEmail(usernameOrEmail));
    }

    @Override
    public User updateUser(Long id, String username, String name, String surname, String email) {
        User existing = repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        existing.setUsername(username);
        existing.setName(name);
        existing.setSurname(surname);
        existing.setEmail(email);
        return repository.save(existing);
    }

    @Override
    public User createUser(String username, String name, String surname, String email, String passwordHash) {
        User user = new User();
        user.setUsername(username);
        user.setName(name);
        user.setSurname(surname);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.addRoleType(RoleType.STUDENT);
        return repository.save(user);
    }

    @Override
    public Boolean deleteUser(Long id) {
        User existing = repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        return repository.delete(existing.getId());
    }
}
