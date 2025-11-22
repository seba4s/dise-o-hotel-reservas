package com.hotel.reservation.exception;

import lombok.Getter;

/**
 * Exception thrown when authentication is required but missing or invalid
 * Used for: invalid tokens, expired sessions, missing credentials, etc.
 */
@Getter
public class UnauthorizedException extends RuntimeException {

    private final String authenticationType;
    private final String reason;

    public UnauthorizedException(String message) {
        super(message);
        this.authenticationType = "GENERAL";
        this.reason = message;
    }

    public UnauthorizedException(String message, String authenticationType, String reason) {
        super(message);
        this.authenticationType = authenticationType;
        this.reason = reason;
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
        this.authenticationType = "GENERAL";
        this.reason = message;
    }

    /**
     * Static factory methods for common authentication issues
     */
    public static UnauthorizedException invalidCredentials() {
        return new UnauthorizedException(
            "Invalid username or password",
            "CREDENTIALS",
            "INVALID_CREDENTIALS"
        );
    }

    public static UnauthorizedException invalidToken() {
        return new UnauthorizedException(
            "Invalid or malformed authentication token",
            "JWT",
            "INVALID_TOKEN"
        );
    }

    public static UnauthorizedException expiredToken() {
        return new UnauthorizedException(
            "Authentication token has expired",
            "JWT",
            "EXPIRED_TOKEN"
        );
    }

    public static UnauthorizedException missingToken() {
        return new UnauthorizedException(
            "Authentication token is required",
            "JWT",
            "MISSING_TOKEN"
        );
    }

    public static UnauthorizedException invalidRefreshToken() {
        return new UnauthorizedException(
            "Invalid or expired refresh token",
            "REFRESH_TOKEN",
            "INVALID_REFRESH_TOKEN"
        );
    }

    public static UnauthorizedException sessionExpired() {
        return new UnauthorizedException(
            "User session has expired",
            "SESSION",
            "SESSION_EXPIRED"
        );
    }

    public static UnauthorizedException accountNotVerified() {
        return new UnauthorizedException(
            "Account email verification is required",
            "EMAIL_VERIFICATION",
            "ACCOUNT_NOT_VERIFIED"
        );
    }
}