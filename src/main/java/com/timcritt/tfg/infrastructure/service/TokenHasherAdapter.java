package com.timcritt.tfg.infrastructure.service;

import com.timcritt.tfg.application.port.outbound.TokenHasherPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@Service
public class TokenHasherAdapter implements TokenHasherPort {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int MIN_SECRET_LENGTH = 16;

    private final byte[] secret;

    public TokenHasherAdapter(
            @Value("${app.tokens.hmac-secret}") String secret
    ) {
        if (secret == null || secret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalArgumentException(
                    "app.tokens.hmac-secret must be set and at least "
                            + MIN_SECRET_LENGTH
                            + " characters long"
            );
        }

        this.secret =
                secret.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String hash(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException(
                    "rawToken must not be blank"
            );
        }

        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);

            mac.init(
                    new SecretKeySpec(
                            secret,
                            HMAC_ALGORITHM
                    )
            );

            byte[] hash =
                    mac.doFinal(
                            rawToken.getBytes(StandardCharsets.UTF_8)
                    );

            return HexFormat.of().formatHex(hash);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to HMAC token",
                    e
            );
        }
    }
}