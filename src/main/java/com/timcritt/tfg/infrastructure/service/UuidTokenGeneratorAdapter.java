package com.timcritt.tfg.infrastructure.service;

import com.timcritt.tfg.application.port.outbound.TokenGeneratorPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UuidTokenGeneratorAdapter implements TokenGeneratorPort {

    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}