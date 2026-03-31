package com.timcritt.tfg.infrastructure.service;

import com.timcritt.tfg.application.port.outbound.PasswordEncoderPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SpringPasswordEncoderAdapter implements PasswordEncoderPort {
    private final PasswordEncoder delegate;

    public SpringPasswordEncoderAdapter(PasswordEncoder delegate) {
        this.delegate = delegate;
    }

    @Override
    public String encode(String rawPassword) {
        return delegate.encode(rawPassword);
    }

    @Override
    public Boolean matches(String rawPassword, String encodedPassword) {
        return delegate.matches(rawPassword, encodedPassword);
    }
}
