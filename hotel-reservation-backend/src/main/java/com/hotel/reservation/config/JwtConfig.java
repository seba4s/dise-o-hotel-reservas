package com.hotel.reservation.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    /**
     * JWT secret key for signing tokens.
     * Must be at least 64 characters (512 bits) for HS512 algorithm.
     */
    private String secret = "hotelReservationSecretKey2025VerySecureAndLongKey";

    /**
     * JWT token expiration time in milliseconds (24 hours)
     */
    private Long expiration = 86_400_000L;

    /**
     * JWT refresh token expiration time in milliseconds (7 days)
     */
    private Long refreshExpiration = 604_800_000L;

    /**
     * JWT token prefix for Authorization header
     */
    private String tokenPrefix = "Bearer";

    /**
     * JWT token header name
     */
    private String headerString = "Authorization";

    /**
     * JWT issuer
     */
    private String issuer = "hotel-reservation-system";

    /**
     * JWT audience
     */
    private String audience = "hotel-users";

    /**
     * Validates JWT configuration on initialization.
     * Ensures the secret key is sufficiently long for HS512 algorithm (minimum 64 characters).
     * This validation helps fail fast if the secret is too short for secure token signing.
     */
    @PostConstruct
    public void validateConfig() {
        if (secret == null || secret.length() < 64) {
            String errorMsg = String.format(
                "JWT secret must be at least 64 characters for HS512 algorithm. Current length: %d",
                secret == null ? 0 : secret.length()
            );
            log.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }
        log.info("JWT configuration validated successfully. Secret length: {} characters", secret.length());
    }
}