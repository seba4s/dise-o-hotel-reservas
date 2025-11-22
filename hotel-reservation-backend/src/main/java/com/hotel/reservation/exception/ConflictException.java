package com.hotel.reservation.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception thrown when there's a conflict with the current state
 * Used for: duplicate resources, business rule conflicts, etc.
 */
@Getter
public class ConflictException extends RuntimeException {

    private final String conflictType;
    private final Map<String, Object> details;

    public ConflictException(String message) {
        super(message);
        this.conflictType = "GENERAL_CONFLICT";
        this.details = new HashMap<>();
    }

    public ConflictException(String message, String conflictType) {
        super(message);
        this.conflictType = conflictType;
        this.details = new HashMap<>();
    }

    public ConflictException(String message, String conflictType, Map<String, Object> details) {
        super(message);
        this.conflictType = conflictType;
        this.details = details != null ? details : new HashMap<>();
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
        this.conflictType = "GENERAL_CONFLICT";
        this.details = new HashMap<>();
    }

    /**
     * Static factory methods for common conflicts
     */
    public static ConflictException userAlreadyExists(String username, String email) {
        return new ConflictException(
            "User already exists with the provided username or email",
            "USER_ALREADY_EXISTS",
            Map.of("username", username, "email", email)
        );
    }

    public static ConflictException usernameAlreadyExists(String username) {
        return new ConflictException(
            String.format("Username '%s' is already taken", username),
            "USERNAME_TAKEN",
            Map.of("username", username)
        );
    }

    public static ConflictException emailAlreadyExists(String email) {
        return new ConflictException(
            String.format("Email '%s' is already registered", email),
            "EMAIL_TAKEN",
            Map.of("email", email)
        );
    }

    public static ConflictException roomAlreadyExists(String roomNumber) {
        return new ConflictException(
            String.format("Room number '%s' already exists", roomNumber),
            "ROOM_ALREADY_EXISTS",
            Map.of("roomNumber", roomNumber)
        );
    }

    public static ConflictException duplicateReservation(String guestId, String roomId, 
                                                        String checkInDate, String checkOutDate) {
        return new ConflictException(
            "Duplicate reservation detected for the same guest, room and dates",
            "DUPLICATE_RESERVATION",
            Map.of(
                "guestId", guestId,
                "roomId", roomId,
                "checkInDate", checkInDate,
                "checkOutDate", checkOutDate
            )
        );
    }

    public static ConflictException accountLocked(String username) {
        return new ConflictException(
            String.format("Account '%s' is locked", username),
            "ACCOUNT_LOCKED",
            Map.of("username", username)
        );
    }

    public static ConflictException accountDisabled(String username) {
        return new ConflictException(
            String.format("Account '%s' is disabled", username),
            "ACCOUNT_DISABLED",
            Map.of("username", username)
        );
    }
}