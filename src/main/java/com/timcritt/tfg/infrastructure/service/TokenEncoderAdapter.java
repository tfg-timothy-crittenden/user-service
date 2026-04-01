package com.timcritt.tfg.infrastructure.service;

import com.timcritt.tfg.application.port.outbound.TokenEncoderPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
public class TokenEncoderAdapter implements TokenEncoderPort {

    private static final String HMAC_ALGO = "HmacSHA256";
    private final byte[] secret;

    public TokenEncoderAdapter(@Value("${app.tokens.hmac-secret}") String secret) {
        if (secret == null || secret.length() < 16) {
            throw new IllegalArgumentException("app.tokens.hmac-secret must be set and sufficiently long");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String encode(String rawToken) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(secret, HMAC_ALGO));
            byte[] h = mac.doFinal(rawToken.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(h);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to HMAC token", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }
}
