package com.hotel.reservation.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception thrown when the client request is invalid
 * Used for: invalid input data, business rule violations, etc.
 */
@Getter
public class BadRequestException extends RuntimeException {

    private final Map<String, Object> details;

    public BadRequestException(String message) {
        super(message);
        this.details = new HashMap<>();
    }

    public BadRequestException(String message, Map<String, Object> details) {
        super(message);
        this.details = details != null ? details : new HashMap<>();
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
        this.details = new HashMap<>();
    }

    public BadRequestException(String message, String field, Object value) {
        super(message);
        this.details = Map.of("field", field, "value", value);
    }

    /**
     * Static factory methods for common validation errors
     */
    public static BadRequestException invalidDateRange(String startDate, String endDate) {
        return new BadRequestException(
            "Invalid date range: check-out date must be after check-in date",
            Map.of("checkInDate", startDate, "checkOutDate", endDate)
        );
    }

    public static BadRequestException pastDate(String field, String date) {
        return new BadRequestException(
            String.format("%s cannot be in the past", field),
            Map.of("field", field, "value", date)
        );
    }

    public static BadRequestException invalidGuestCount(Integer adults, Integer children) {
        return new BadRequestException(
            "Invalid guest count: at least 1 adult is required",
            Map.of("adults", adults, "children", children)
        );
    }

    public static BadRequestException exceedsMaxStayDuration(Long nights, Long maxNights) {
        return new BadRequestException(
            String.format("Stay duration exceeds maximum allowed: %d nights", maxNights),
            Map.of("requestedNights", nights, "maxNights", maxNights)
        );
    }

    public static BadRequestException invalidRoomCapacity(Integer requestedGuests, Integer roomCapacity) {
        return new BadRequestException(
            String.format("Room capacity exceeded: requested %d guests, capacity %d", 
                         requestedGuests, roomCapacity),
            Map.of("requestedGuests", requestedGuests, "roomCapacity", roomCapacity)
        );
    }

    public static BadRequestException passwordMismatch() {
        return new BadRequestException(
            "Passwords do not match",
            Map.of("field", "confirmPassword")
        );
    }

    public static BadRequestException invalidDocumentType(String documentType) {
        return new BadRequestException(
            "Invalid document type",
            Map.of("documentType", documentType, "validTypes", 
                   new String[]{"PASSPORT", "NATIONAL_ID", "DRIVER_LICENSE", "OTHER"})
        );
    }

    public static BadRequestException missingRequiredField(String fieldName) {
        return new BadRequestException(
            String.format("Required field '%s' is missing or empty", fieldName),
            Map.of("field", fieldName)
        );
    }
}