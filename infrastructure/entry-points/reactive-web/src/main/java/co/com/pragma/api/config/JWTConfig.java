package co.com.pragma.api.config;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Configuration
public class JWTConfig {

    private final String jwtSecret;
    private final Long jwtExpiration;

    public JWTConfig(@Value("${jwt.secret}") String jwtSecret,
                     @Value("${jwt.expiration}") Long jwtExpiration) {
        this.jwtSecret = jwtSecret;
        this.jwtExpiration = jwtExpiration;
    }

    public SecretKey secretKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public Long getJwtExpiration() {
        return jwtExpiration;
    }
}
