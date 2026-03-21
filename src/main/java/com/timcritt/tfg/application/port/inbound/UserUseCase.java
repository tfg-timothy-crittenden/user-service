package com.timcritt.tfg.application.port.inbound;

import com.timcritt.tfg.domain.model.User;

// This interface defines the contract for user use cases implemented by the application service.
public interface UserUseCase {
    User createUser(String username, String name, String surname, String email);
    User getUserById(Long id);
    User updateUser(Long id, String username, String name, String surname, String email);
    Boolean deleteUser(Long id);
}
