package com.timcritt.tfg.infrastructure.service;

import com.timcritt.tfg.application.port.inbound.UserUseCase;
import com.timcritt.tfg.application.port.outbound.RoleEventPublisherPort;
import com.timcritt.tfg.application.port.outbound.UserRepositoryPort;
import com.timcritt.tfg.application.service.UserUseCaseService;
import com.timcritt.tfg.domain.event.TeacherRoleRevokedEvent;
import com.timcritt.tfg.domain.model.RoleType;
import com.timcritt.tfg.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceAdapter implements UserUseCase {

    private final UserUseCaseService delegate;
    private final EmailVerificationAdapter emailVerificationFacade;
    private final RoleEventPublisherPort roleEventPublisher;

    public UserServiceAdapter(UserRepositoryPort repository,
                              EmailVerificationAdapter emailVerificationFacade,
                              RoleEventPublisherPort roleEventPublisher) {
        this.delegate = new UserUseCaseService(repository);
        this.emailVerificationFacade = emailVerificationFacade;
        this.roleEventPublisher = roleEventPublisher;
    }

    @Override
    @Transactional
    public User createUser(String username, String name, String surname, String email, String passwordHash) {
        User saved = delegate.createUser(username, name, surname, email, passwordHash);
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

    @Override
    @Transactional
    public User removeRole(Long userId, RoleType roleType) {
        User user = delegate.removeRole(userId, roleType);
        if (roleType == RoleType.TEACHER) {
            roleEventPublisher.publishTeacherRoleRevoked(new TeacherRoleRevokedEvent(userId));
        }
        return user;
    }
}
