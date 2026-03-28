package com.timcritt.tfg.application.port.outbound;

import com.timcritt.tfg.domain.model.User;

import javax.swing.text.html.Option;

import java.util.Optional;

// This interface defines the contract for the repository used by the application service.
public interface UserRepositoryPort {
    Optional<User> findById(Long id);
    User save(User user);
    Boolean delete(Long id);
    Optional<User> findByUsername(String username);
}
