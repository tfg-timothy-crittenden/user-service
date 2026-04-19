package com.timcritt.tfg.application.port.inbound;

import com.timcritt.tfg.domain.model.RoleType;
import com.timcritt.tfg.domain.model.User;

import java.util.List;
import java.util.Optional;

// This interface defines the contract for user use cases implemented by the application service.
public interface UserUseCase {
    User createUser(String username, String name, String surname, String email, String passwordHash);
    User getUserById(Long id);
    User getUserByUsername(String username);
    User updateUser(Long id, String username, String name, String surname, String email);
    Boolean deleteUser(Long id);
    List<User> getAllUsersByRoleType(RoleType role);
    // Attempt to find a user by username or email; returns Optional.empty() if not found.
    Optional<User> findByIdentifier(String usernameOrEmail);
}
