package com.timcritt.tfg.application.service;

import com.timcritt.tfg.application.exception.UserNotFoundException;
import com.timcritt.tfg.application.port.inbound.UserUseCase;
import com.timcritt.tfg.application.port.outbound.UserRepositoryPort;
import com.timcritt.tfg.domain.model.User;

// This class contains business logic for handling User operations.
public class UserUseCaseImpl implements UserUseCase {

    private final UserRepositoryPort repository;

    public UserUseCaseImpl(UserRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public User getUserById(Long id) {
        return repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
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
    public User createUser(String username, String name, String surname, String email) {
        User user = new User();
        user.setUsername(username);
        user.setName(name);
        user.setSurname(surname);
        user.setEmail(email);
        return repository.save(user);
    }

    @Override
    public Boolean deleteUser(Long id) {
        User existing = repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        return repository.delete(existing.getId());
    }
}
