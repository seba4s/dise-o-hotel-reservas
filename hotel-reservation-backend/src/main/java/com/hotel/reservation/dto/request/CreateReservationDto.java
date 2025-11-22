package com.hotel.reservation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Create reservation request data - HU004")
public class CreateReservationDto {

    @Schema(description = "Room ID to reserve", example = "64abc123def456789012", required = true)
    @NotBlank(message = "Room ID is required")
    private String roomId;

    @Schema(description = "Check-in date", example = "2025-11-25", required = true)
    @NotNull(message = "Check-in date is required")
    @Future(message = "Check-in date must be in the future")
    private LocalDate checkInDate;

    @Schema(description = "Check-out date", example = "2025-11-28", required = true)
    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be in the future")
    private LocalDate checkOutDate;

    @Schema(description = "Number of adults", example = "2", required = true, minimum = "1", maximum = "8")
    @NotNull(message = "Number of adults is required")
    @Positive(message = "Number of adults must be at least 1")
    @Min(value = 1, message = "At least 1 adult is required")
    @Max(value = 8, message = "Maximum 8 adults per room")
    private Integer adults;

    @Schema(description = "Number of children", example = "1", required = false, minimum = "0", maximum = "6")
    @Min(value = 0, message = "Number of children cannot be negative")
    @Max(value = 6, message = "Maximum 6 children per room")
    @Builder.Default
    private Integer children = 0;

    // Guest information (for new guests or updates)
    @Schema(description = "Guest email address", example = "guest@example.com", required = false)
    @Email(message = "Guest email should be valid")
    private String guestEmail;

    @Schema(description = "Guest first name", example = "Ana", required = false)
    @Size(min = 2, max = 50, message = "Guest first name must be between 2 and 50 characters")
    private String guestFirstName;

    @Schema(description = "Guest last name", example = "López", required = false)
    @Size(min = 2, max = 50, message = "Guest last name must be between 2 and 50 characters")
    private String guestLastName;

    @Schema(description = "Guest phone number", example = "+57 300 987 6543", required = false)
    @Pattern(regexp = "^[\\+]?[1-9][\\d\\s\\-\\(\\)]{8,15}$", 
             message = "Phone number format is invalid")
    private String guestPhone;

    @Schema(description = "Guest country", example = "CO", required = false)
    @Size(min = 2, max = 3, message = "Country code must be 2 or 3 characters")
    private String guestCountry;

    @Schema(description = "Special requests or notes", example = "Late check-in, ground floor preferred", required = false)
    @Size(max = 500, message = "Special requests must not exceed 500 characters")
    private String specialRequests;

    @Schema(description = "Preferred payment method", example = "CREDIT_CARD", required = false,
            allowableValues = {"CREDIT_CARD", "DEBIT_CARD", "CASH", "BANK_TRANSFER"})
    private String paymentMethod;

    @Schema(description = "Promotional code", example = "SUMMER2025", required = false)
    @Size(max = 20, message = "Promotional code must not exceed 20 characters")
    private String promoCode;

    @Schema(description = "Rate plan preference", example = "FLEXIBLE", required = false,
            allowableValues = {"FLEXIBLE", "SEMI_FLEXIBLE", "NON_REFUNDABLE"})
    @Builder.Default
    private String ratePlan = "FLEXIBLE";

    @Schema(description = "Include breakfast", example = "true", required = false)
    @Builder.Default
    private Boolean includeBreakfast = false;

    @Schema(description = "Airport transfer required", example = "false", required = false)
    @Builder.Default
    private Boolean airportTransfer = false;

    /**
     * Validates that check-out date is after check-in date
     */
    @AssertTrue(message = "Check-out date must be after check-in date")
    public boolean isValidDateRange() {
        if (checkInDate == null || checkOutDate == null) {
            return true; // Let @NotNull handle null validation
        }
        return checkOutDate.isAfter(checkInDate);
    }

    /**
     * Validates maximum stay duration (e.g., 30 days)
     */
    @AssertTrue(message = "Maximum stay duration is 30 days")
    public boolean isValidStayDuration() {
        if (checkInDate == null || checkOutDate == null) {
            return true;
        }
        return checkInDate.plusDays(30).isAfter(checkOutDate) || 
               checkInDate.plusDays(30).isEqual(checkOutDate);
    }

    /**
     * Validates total guests capacity
     */
    @AssertTrue(message = "Total guests cannot exceed 10 per room")
    public boolean isValidGuestCount() {
        if (adults == null) {
            return true;
        }
        int totalGuests = adults + (children != null ? children : 0);
        return totalGuests <= 10;
    }

    /**
     * Calculate total number of guests
     */
    public Integer getTotalGuests() {
        return adults + (children != null ? children : 0);
    }

    /**
     * Calculate number of nights
     */
    public Long getNights() {
        if (checkInDate == null || checkOutDate == null) {
            return null;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    /**
     * Check if guest information is provided
     */
    public boolean hasGuestInfo() {
        return guestEmail != null || guestFirstName != null || guestLastName != null;
    }
}