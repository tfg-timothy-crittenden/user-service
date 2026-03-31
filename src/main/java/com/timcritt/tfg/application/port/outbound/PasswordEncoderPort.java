package com.timcritt.tfg.application.port.outbound;

public interface PasswordEncoderPort {
    String encode(String password);
    boolean matches(String password, String encodedPassword);
}
