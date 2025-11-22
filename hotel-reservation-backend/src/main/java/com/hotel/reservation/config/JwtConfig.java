package com.hotel.reservation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    /**
     * JWT secret key for signing tokens
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
    private String tokenPrefix = "Bearer ";

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
}