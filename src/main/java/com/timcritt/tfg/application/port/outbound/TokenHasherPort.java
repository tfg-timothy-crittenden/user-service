package com.timcritt.tfg.application.port.outbound;

public interface TokenHasherPort {
    String hash(String token);
}
