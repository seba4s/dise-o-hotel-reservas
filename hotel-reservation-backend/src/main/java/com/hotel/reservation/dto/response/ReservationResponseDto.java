package com.hotel.reservation.dto.response;

import com.hotel.reservation.model.Reservation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Reservation information response - HU004")
public class ReservationResponseDto {

    @Schema(description = "Reservation unique identifier", example = "64abc123def456789012", required = true)
    private String id;

    @Schema(description = "Confirmation number", example = "CONF73829456", required = true)
    private String confirmationNumber;

    @Schema(description = "Guest information", required = true)
    private GuestDto guest;

    @Schema(description = "Room information", required = true)
    private RoomDto room;

    @Schema(description = "Check-in date", example = "2025-11-25", required = true)
    private LocalDate checkInDate;

    @Schema(description = "Check-out date", example = "2025-11-28", required = true)
    private LocalDate checkOutDate;

    @Schema(description = "Number of adults", example = "2", required = true)
    private Integer adults;

    @Schema(description = "Number of children", example = "1", required = true)
    private Integer children;

    @Schema(description = "Total reservation amount", example = "1500000.00", required = true)
    private BigDecimal totalAmount;

    @Schema(description = "Currency code", example = "COP", required = true)
    @Builder.Default
    private String currency = "COP";

    @Schema(description = "Reservation status", example = "CONFIRMED", required = true)
    private Reservation.ReservationStatus status;

    @Schema(description = "Special requests from guest", 
            example = "Late check-in, ground floor preferred", required = false)
    private String specialRequests;

    @Schema(description = "Number of nights", example = "3", required = true)
    private Long nights;

    @Schema(description = "Price breakdown", required = false)
    private PricingDto pricing;

    // Check-in/out tracking
    @Schema(description = "Actual check-in timestamp", example = "2025-11-25T15:30:00", required = false)
    private LocalDateTime actualCheckInTime;

    @Schema(description = "Actual check-out timestamp", example = "2025-11-28T11:00:00", required = false)
    private LocalDateTime actualCheckOutTime;

    @Schema(description = "Staff member who processed check-in", example = "staff_user", required = false)
    private String checkInStaff;

    @Schema(description = "Staff member who processed check-out", example = "staff_user", required = false)
    private String checkOutStaff;

    // Additional charges during stay
    @Schema(description = "Additional charges during stay", required = false)
    private List<AdditionalChargeDto> additionalCharges;

    // Payment information
    @Schema(description = "Payment method used", example = "CREDIT_CARD", required = false)
    private String paymentMethod;

    @Schema(description = "Payment status", example = "PAID", required = false)
    private String paymentStatus;

    @Schema(description = "Payment transaction ID", example = "txn_12345abc", required = false)
    private String paymentTransactionId;

    @Schema(description = "Promotional code used", example = "SUMMER2025", required = false)
    private String promoCode;

    @Schema(description = "Rate plan", example = "FLEXIBLE", required = false)
    private String ratePlan;

    @Schema(description = "Includes breakfast", example = "true", required = false)
    private Boolean includeBreakfast;

    @Schema(description = "Airport transfer requested", example = "false", required = false)
    private Boolean airportTransfer;

    // Timestamps
    @Schema(description = "Reservation creation date", example = "2025-11-22T03:51:37", required = true)
    private LocalDateTime createdAt;

    @Schema(description = "Last modification date", example = "2025-11-22T03:51:37", required = true)
    private LocalDateTime updatedAt;

    @Schema(description = "Cancellation date", example = "2025-11-23T10:15:00", required = false)
    private LocalDateTime cancelledAt;

    @Schema(description = "Cancellation reason", example = "Guest request", required = false)
    private String cancellationReason;

    /**
     * Guest information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Guest details")
    public static class GuestDto {
        
        @Schema(description = "Guest ID", example = "64def456abc789012345")
        private String id;
        
        @Schema(description = "Guest email", example = "ana.lopez@example.com")
        private String email;
        
        @Schema(description = "First name", example = "Ana")
        private String firstName;
        
        @Schema(description = "Last name", example = "López")
        private String lastName;
        
        @Schema(description = "Phone number", example = "+57 300 987 6543")
        private String phone;
        
        @Schema(description = "Country", example = "CO")
        private String country;

        public String getFullName() {
            return String.format("%s %s", firstName, lastName);
        }
    }

    /**
     * Room information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Room details")
    public static class RoomDto {
        
        @Schema(description = "Room ID", example = "64abc789def012345678")
        private String id;
        
        @Schema(description = "Room number", example = "201")
        private String roomNumber;
        
        @Schema(description = "Room type", example = "Deluxe Vista al Mar")
        private String roomType;
        
        @Schema(description = "Room capacity", example = "4")
        private Integer capacity;
        
        @Schema(description = "Bed type", example = "King Size")
        private String bedType;
        
        @Schema(description = "Base price per night", example = "500000.00")
        private BigDecimal basePrice;
        
        @Schema(description = "Room amenities", example = "[\"WiFi\", \"Ocean View\", \"Minibar\"]")
        private List<String> amenities;
    }

    /**
     * Additional charge during stay
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Additional charge details")
    public static class AdditionalChargeDto {
        
        @Schema(description = "Charge description", example = "Minibar consumption")
        private String description;
        
        @Schema(description = "Charge amount", example = "25000.00")
        private BigDecimal amount;
        
        @Schema(description = "Charge type", example = "MINIBAR")
        private String type;
        
        @Schema(description = "Date when charge was applied", example = "2025-11-26T20:30:00")
        private LocalDateTime addedAt;
        
        @Schema(description = "Staff member who added the charge", example = "staff_user")
        private String addedBy;
    }

    /**
     * Pricing breakdown
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Pricing breakdown details")
    public static class PricingDto {
        
        @Schema(description = "Room subtotal", example = "1500000.00")
        private BigDecimal roomSubtotal;
        
        @Schema(description = "Taxes amount", example = "285000.00")
        private BigDecimal taxes;
        
        @Schema(description = "Service fees", example = "50000.00")
        private BigDecimal serviceFees;
        
        @Schema(description = "Discount amount", example = "75000.00")
        private BigDecimal discounts;
        
        @Schema(description = "Additional services", example = "30000.00")
        private BigDecimal additionalServices;
        
        @Schema(description = "Final total", example = "1790000.00")
        private BigDecimal finalTotal;
        
        @Schema(description = "Amount paid", example = "1790000.00")
        private BigDecimal amountPaid;
        
        @Schema(description = "Balance due", example = "0.00")
        private BigDecimal balanceDue;
    }

    /**
     * Calculate total guests
     */
    public Integer getTotalGuests() {
        return adults + (children != null ? children : 0);
    }

    /**
     * Calculate nights between dates
     */
    public Long calculateNights() {
        if (checkInDate == null || checkOutDate == null) return null;
        return java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    /**
     * Check if reservation is active
     */
    public Boolean isActive() {
        return status == Reservation.ReservationStatus.CONFIRMED || 
               status == Reservation.ReservationStatus.CHECKED_IN;
    }

    /**
     * Check if reservation can be cancelled
     */
    public Boolean canBeCancelled() {
        return status == Reservation.ReservationStatus.PRE_RESERVATION || 
               status == Reservation.ReservationStatus.CONFIRMED;
    }

    /**
     * Check if reservation can be modified
     */
    public Boolean canBeModified() {
        return status == Reservation.ReservationStatus.PRE_RESERVATION;
    }

    /**
     * Get total additional charges amount
     */
    public BigDecimal getTotalAdditionalCharges() {
        if (additionalCharges == null || additionalCharges.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return additionalCharges.stream()
                .map(AdditionalChargeDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get reservation progress status
     */
    public String getProgressStatus() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        
        if (status == Reservation.ReservationStatus.CHECKED_OUT) {
            return "COMPLETED";
        } else if (status == Reservation.ReservationStatus.CHECKED_IN) {
            return "IN_HOUSE";
        } else if (status == Reservation.ReservationStatus.CONFIRMED && checkInDate.equals(today)) {
            return "ARRIVING_TODAY";
        } else if (status == Reservation.ReservationStatus.CONFIRMED && checkOutDate.equals(today)) {
            return "DEPARTING_TODAY";
        } else if (status == Reservation.ReservationStatus.CONFIRMED) {
            return "CONFIRMED";
        } else {
            return status.toString();
        }
    }
}