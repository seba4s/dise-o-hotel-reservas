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
@Schema(description = "Room availability search criteria - HU001")
public class AvailabilitySearchDto {

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

    @Schema(description = "Preferred room type", example = "Deluxe", required = false, 
            allowableValues = {"Standard", "Deluxe", "Suite"})
    private String roomType;

    @Schema(description = "Price range preference", example = "MEDIUM", required = false,
            allowableValues = {"LOW", "MEDIUM", "HIGH", "LUXURY"})
    private String priceRange;

    @Schema(description = "Special amenities filter", example = "WiFi,Pool,Spa", required = false)
    private String amenities;

    @Schema(description = "Accessibility requirements", example = "true", required = false)
    private Boolean accessibilityRequired;

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
}