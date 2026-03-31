// java
package com.timcritt.tfg.infrastructure.security;

import com.timcritt.tfg.domain.model.User;
import com.timcritt.tfg.domain.model.Role;
import com.timcritt.tfg.application.port.outbound.UserRepositoryPort;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepositoryPort userRepository;

    public CustomUserDetailsService(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        var authorities = user.getRoles().stream()
                .map(Role::getRoleType)
                .map(Enum::name)
                .map(r -> "ROLE_" + r) // add ROLE_ prefix to satisfy hasRole(...)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new CustomUserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                user.isVerified(),
                authorities
        );
    }
}
