package com.timcritt.tfg.infrastructure.service;

import com.timcritt.tfg.application.port.inbound.UserUseCase;
import com.timcritt.tfg.application.port.outbound.UserRepositoryPort;
import com.timcritt.tfg.application.service.UserUseCaseImpl;
import com.timcritt.tfg.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// This class serves as an adapter that connects the application service implementation (UserUseCaseImpl)
// to the Spring framework. It implements the UserUseCase interface and delegates the actual business logic
// to the UserUseCaseImpl class. The @Service annotation indicates that this class is a Spring-managed component,
// and the @Transactional annotations ensure methods run inside a transactional context.

@Service
public class UserServiceAdapter implements UserUseCase {

    private final UserUseCaseImpl delegate;

    public UserServiceAdapter(UserRepositoryPort repository) {
        this.delegate = new UserUseCaseImpl(repository);
    }

    @Override
    @Transactional
    public User createUser(String username, String name, String surname, String email) {
        return delegate.createUser(username, name, surname, email);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return delegate.getUserById(id);
    }

    @Override
    @Transactional
    public User updateUser(Long id, String username, String name, String surname, String email) {
        return delegate.updateUser(id, username, name, surname, email);
    }

    @Override
    @Transactional
    public Boolean deleteUser(Long id) {
        return delegate.deleteUser(id);
    }
}
