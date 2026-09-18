package com.timcritt.tfg.infrastructure.service;

import com.timcritt.tfg.application.port.inbound.EmailVerificationUseCase;
import com.timcritt.tfg.application.port.inbound.UserUseCase;
import com.timcritt.tfg.application.port.outbound.UserEventPublisherPort;
import com.timcritt.tfg.application.port.outbound.UserRepositoryPort;
import com.timcritt.tfg.application.service.UserUseCaseService;
import com.timcritt.tfg.domain.event.TeacherRoleRevokedEvent;
import com.timcritt.tfg.domain.model.Role;
import com.timcritt.tfg.domain.model.aggregate.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceAdapter implements UserUseCase {

    private final UserUseCaseService delegate;
    private final UserEventPublisherPort userEventPublisher;
    private final EmailVerificationUseCase emailVerificationUseCase;

    public UserServiceAdapter(
            UserRepositoryPort repository,
            EmailVerificationUseCase emailVerificationUseCase,
            UserEventPublisherPort userEventPublisher
    ) {
        this.delegate = new UserUseCaseService(
                repository,
                emailVerificationUseCase,
                userEventPublisher
        );

        this.emailVerificationUseCase = emailVerificationUseCase;
        this.userEventPublisher = userEventPublisher;
    }

    @Override
    @Transactional
    public User createUser(
            String username,
            String name,
            String surname,
            String email,
            String password
    ) {
        User saved = delegate.createUser(
                username,
                name,
                surname,
                email,
                password
        );

        emailVerificationUseCase.createAndSendToken(
                saved.getId(),
                saved.getEmail()
        );

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return delegate.getUserByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return delegate.getUserById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByIdentifier(String usernameOrEmail) {
        return delegate.findByIdentifier(usernameOrEmail);
    }

    @Override
    @Transactional
    public User updateUser(
            Long id,
            String username,
            String name,
            String surname,
            String email
    ) {
        return delegate.updateUser(
                id,
                username,
                name,
                surname,
                email
        );
    }

    @Override
    @Transactional
    public Boolean deleteUser(Long id) {
        return delegate.deleteUser(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsersByRole(Role role) {
        return delegate.getAllUsersByRole(role);
    }

    @Override
    @Transactional
    public User removeRole(Long userId, Role role) {
        User user = delegate.removeRole(userId, role);

        if (role == Role.TEACHER) {
            userEventPublisher.publishTeacherRoleRevoked(
                    new TeacherRoleRevokedEvent(userId)
            );
        }

        return user;
    }
}