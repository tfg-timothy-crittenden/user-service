package com.timcritt.tfg.infrastructure.service;

import com.timcritt.tfg.application.port.inbound.UserUseCase;
import com.timcritt.tfg.application.port.outbound.UserRepositoryPort;
import com.timcritt.tfg.application.service.UserUseCaseService;
import com.timcritt.tfg.domain.model.RoleType;
import com.timcritt.tfg.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// This class serves as an adapter that connects the application service implementation (UserUseCaseImpl)
// to the Spring framework. It implements the UserUseCase interface and delegates the actual business logic
// to the UserUseCaseImpl class. The @Service annotation indicates that this class is a Spring-managed component,
// and the @Transactional annotations ensure methods run inside a transactional context.

@Service
public class UserServiceAdapter implements UserUseCase {

    private final UserUseCaseService delegate;
    private final EmailVerificationAdapter emailVerificationFacade;

    public UserServiceAdapter(UserRepositoryPort repository, EmailVerificationAdapter emailVerificationFacade) {
        this.delegate = new UserUseCaseService(repository);
        this.emailVerificationFacade = emailVerificationFacade;
    }

    @Override
    @Transactional
    public User createUser(String username, String name, String surname, String email, String passwordHash) {
        User saved = delegate.createUser(username, name, surname, email, passwordHash);
        // generate verification token and send email
        emailVerificationFacade.createAndSendToken(saved.getId(), saved.getEmail());
        return saved;
    }

    @Override
    @Transactional
    public User getUserByUsername(String username) {
        return delegate.getUserByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return delegate.getUserById(id);
    }

    @Override
    public Optional<User> findByIdentifier(String usernameOrEmail) {
        return delegate.findByIdentifier(usernameOrEmail);
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

    @Override
    @Transactional
    public List<User> getAllUsersByRoleType(RoleType role) {
        return delegate.getAllUsersByRoleType(role);
    }
}
