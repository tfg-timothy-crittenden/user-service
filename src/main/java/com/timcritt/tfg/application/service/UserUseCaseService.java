package com.timcritt.tfg.application.service;

import com.timcritt.tfg.application.exception.RoleNotFoundException;
import com.timcritt.tfg.application.exception.UserNotFoundException;
import com.timcritt.tfg.application.port.inbound.EmailVerificationUseCase;
import com.timcritt.tfg.application.port.inbound.UserUseCase;
import com.timcritt.tfg.application.port.outbound.UserEventPublisherPort;
import com.timcritt.tfg.application.port.outbound.UserRepositoryPort;
import com.timcritt.tfg.domain.event.UserProfileUpdatedEvent;
import com.timcritt.tfg.domain.model.Role;
import com.timcritt.tfg.application.exception.UserAlreadyExistsException;
import com.timcritt.tfg.domain.model.aggregate.user.PasswordHash;
import com.timcritt.tfg.domain.model.aggregate.user.User;

import java.util.List;
import java.util.Optional;

public class UserUseCaseService implements UserUseCase {

    private final UserRepositoryPort repository;
    private final EmailVerificationUseCase emailVerificationService;
    private final UserEventPublisherPort userEventPublisher;

    public UserUseCaseService(UserRepositoryPort repository, EmailVerificationUseCase emailVerificationService, UserEventPublisherPort userEventPublisher) {
        this.repository = repository;
        this.emailVerificationService = emailVerificationService;
        this.userEventPublisher = userEventPublisher;

    }


    @Override
    public User getUserById(Long id) {
        return repository.findById(id).orElseThrow(() -> new UserNotFoundException(id, ""));
    }

    @Override
    public User getUserByUsername(String username) {
        // Try username first, then fall back to email lookup for convenience
        return repository.findByUsername(username)
                .or(() -> repository.findByEmail(username))
                .orElseThrow(() -> new UserNotFoundException(username, null));
    }

    @Override
    public Optional<User> findByIdentifier(String usernameOrEmail) {
        return repository.findByUsername(usernameOrEmail)
                .or(() -> repository.findByEmail(usernameOrEmail));
    }

    @Override
    public User updateUser(Long id, String username, String name, String surname, String email) {
        User existingUser = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id, ""));

        boolean emailChanged =
                !existingUser.getEmail().equalsIgnoreCase(email);

        repository.findByUsername(username)
                .filter(found -> !found.getId().equals(id))
                .ifPresent(found -> {
                    throw new UserAlreadyExistsException(
                            username,
                            "username"
                    );
                });

        repository.findByEmail(email)
                .filter(found -> !found.getId().equals(id))
                .ifPresent(found -> {
                    throw new UserAlreadyExistsException(
                            email,
                            "email"
                    );
                });

        existingUser.updateProfile(username, name, surname);
        existingUser.changeEmail(email);


        User updated = repository.save(existingUser);

        userEventPublisher.publishUserProfileUpdated(
                new UserProfileUpdatedEvent(
                        updated.getId(),
                        updated.getVersion(),
                        updated.getName(),
                        updated.getSurname()
                )
        );

        if (emailChanged) {
            emailVerificationService.createAndSendToken(
                    updated.getId(),
                    updated.getEmail()
            );
        }

        return updated;
    }

    @Override
    public User createUser(String username, String name, String surname, String email, String passwordHash) {
        // Prevent duplicate usernames or emails
        if (repository.findByUsername(username).isPresent()) {
            throw new UserAlreadyExistsException(username, "username");
        }
        if (repository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException(email, "email");
        }

        User newUser = User.createStudent(username, name, surname, email, PasswordHash.of(passwordHash));

        return repository.save(newUser);
    }

    @Override
    public Boolean deleteUser(Long id) {
        User existing = repository.findById(id).orElseThrow(() -> new UserNotFoundException(id, ""));
        return repository.delete(existing.getId());
    }

    @Override
    public List<User> getAllUsersByRole(Role role) {
        return repository.findAllUsersByRole(role);
    }

    @Override
    public User removeRole(Long userId, Role roleToRemove) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId, ""));

        if (!user.hasRole(roleToRemove)) {
            throw new RoleNotFoundException(userId, roleToRemove.name());
        }

        user.revokeRole(roleToRemove);
        return repository.save(user);
    }
}
