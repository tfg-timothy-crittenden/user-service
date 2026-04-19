package com.timcritt.tfg.application.port.outbound;

import com.timcritt.tfg.domain.model.RoleType;
import com.timcritt.tfg.domain.model.User;

import java.util.List;
import java.util.Optional;

// This interface defines the contract for the repository used by the application service.
public interface UserRepositoryPort {
    Optional<User> findById(Long id);
    User save(User user);
    Boolean delete(Long id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findAllUsersByRoleType(RoleType roleType);
}
