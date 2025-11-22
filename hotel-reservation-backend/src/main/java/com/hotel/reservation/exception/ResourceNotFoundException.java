package com.hotel.reservation.exception;

import lombok.Getter;

/**
 * Exception thrown when a requested resource is not found
 * Used for: rooms, reservations, users, etc.
 */
@Getter
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceType;
    private final String resourceId;
    private final String field;

    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceType = null;
        this.resourceId = null;
        this.field = null;
    }

    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(String.format("%s not found with id: %s", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.field = "id";
    }

    public ResourceNotFoundException(String resourceType, String field, String value) {
        super(String.format("%s not found with %s: %s", resourceType, field, value));
        this.resourceType = resourceType;
        this.resourceId = value;
        this.field = field;
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.resourceType = null;
        this.resourceId = null;
        this.field = null;
    }

    /**
     * Static factory methods for common cases
     */
    public static ResourceNotFoundException room(String roomId) {
        return new ResourceNotFoundException("Room", roomId);
    }

    public static ResourceNotFoundException roomByNumber(String roomNumber) {
        return new ResourceNotFoundException("Room", "roomNumber", roomNumber);
    }

    public static ResourceNotFoundException reservation(String reservationId) {
        return new ResourceNotFoundException("Reservation", reservationId);
    }

    public static ResourceNotFoundException reservationByConfirmation(String confirmationNumber) {
        return new ResourceNotFoundException("Reservation", "confirmationNumber", confirmationNumber);
    }

    public static ResourceNotFoundException user(String userId) {
        return new ResourceNotFoundException("User", userId);
    }

    public static ResourceNotFoundException userByUsername(String username) {
        return new ResourceNotFoundException("User", "username", username);
    }

    public static ResourceNotFoundException userByEmail(String email) {
        return new ResourceNotFoundException("User", "email", email);
    }
}