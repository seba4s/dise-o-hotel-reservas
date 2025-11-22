package com.hotel.reservation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Check-in process response - HU009")
public class CheckInResponseDto {

    @Schema(description = "Check-in process ID", example = "checkin_64abc123", required = true)
    private String checkInId;

    @Schema(description = "Reservation ID", example = "64abc123def456789012", required = true)
    private String reservationId;

    @Schema(description = "Confirmation number", example = "CONF73829456", required = true)
    private String confirmationNumber;

    @Schema(description = "Guest information", required = true)
    private GuestDto guest;

    @Schema(description = "Room assignment", required = true)
    private RoomAssignmentDto room;

    @Schema(description = "Check-in timestamp", example = "2025-11-25T15:30:00", required = true)
    private LocalDateTime checkInTime;

    @Schema(description = "Expected check-out date", example = "2025-11-28", required = true)
    private java.time.LocalDate expectedCheckOutDate;

    @Schema(description = "Staff member who processed check-in", example = "seba4s", required = true)
    private String processedBy;

    @Schema(description = "Document verification details", required = true)
    private DocumentVerificationDto documentVerification;

    @Schema(description = "Services requested during check-in", required = false)
    private List<ServiceDto> services;

    @Schema(description = "Deposit information", required = false)
    private DepositDto deposit;

    @Schema(description = "Key cards issued", required = true)
    private KeyCardDto keyCards;

    @Schema(description = "Check-in notes from staff", 
            example = "Guest arrived early, room was ready", required = false)
    private String notes;

    @Schema(description = "Welcome package provided", example = "true", required = false)
    @Builder.Default
    private Boolean welcomePackageProvided = false;

    @Schema(description = "Hotel policies acknowledged", example = "true", required = true)
    private Boolean policiesAcknowledged;

    @Schema(description = "Parking information", required = false)
    private ParkingDto parking;

    @Schema(description = "Emergency contact verified", example = "true", required = false)
    private Boolean emergencyContactVerified;

    @Schema(description = "Check-in status", example = "COMPLETED", required = true)
    @Builder.Default
    private String status = "COMPLETED";

    /**
     * Guest information for check-in
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Guest check-in details")
    public static class GuestDto {
        
        @Schema(description = "Guest full name", example = "Ana López")
        private String fullName;
        
        @Schema(description = "Guest email", example = "ana.lopez@example.com")
        private String email;
        
        @Schema(description = "Guest phone", example = "+57 300 987 6543")
        private String phone;
        
        @Schema(description = "Number of adults", example = "2")
        private Integer adults;
        
        @Schema(description = "Number of children", example = "1")
        private Integer children;
        
        @Schema(description = "VIP guest status", example = "false")
        @Builder.Default
        private Boolean vipGuest = false;
        
        @Schema(description = "Guest preferences", example = "[\"Ground floor\", \"Non-smoking\"]")
        private List<String> preferences;
    }

    /**
     * Room assignment information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Room assignment details")
    public static class RoomAssignmentDto {
        
        @Schema(description = "Room number", example = "201")
        private String roomNumber;
        
        @Schema(description = "Room type", example = "Deluxe Vista al Mar")
        private String roomType;
        
        @Schema(description = "Floor number", example = "2")
        private Integer floor;
        
        @Schema(description = "Room features", example = "[\"Ocean View\", \"Balcony\", \"King Bed\"]")
        private List<String> features;
        
        @Schema(description = "WiFi network name", example = "Hotel_Guest_201")
        private String wifiNetwork;
        
        @Schema(description = "WiFi password", example = "Welcome2025!")
        private String wifiPassword;
        
        @Schema(description = "Room rate for this stay", example = "500000.00")
        private BigDecimal roomRate;
    }

    /**
     * Document verification details
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Document verification information")
    public static class DocumentVerificationDto {
        
        @Schema(description = "Document type verified", example = "PASSPORT")
        private String documentType;
        
        @Schema(description = "Document number", example = "AB123456789")
        private String documentNumber;
        
        @Schema(description = "Document issuing country", example = "CO")
        private String issuingCountry;
        
        @Schema(description = "Verification status", example = "VERIFIED")
        private String verificationStatus;
        
        @Schema(description = "Verification timestamp", example = "2025-11-25T15:25:00")
        private LocalDateTime verifiedAt;
        
        @Schema(description = "Document copy stored", example = "true")
        @Builder.Default
        private Boolean documentCopyStored = true;
    }

    /**
     * Service information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Service details")
    public static class ServiceDto {
        
        @Schema(description = "Service name", example = "Breakfast")
        private String name;
        
        @Schema(description = "Service description", example = "Continental breakfast included")
        private String description;
        
        @Schema(description = "Service cost", example = "25000.00")
        private BigDecimal cost;
        
        @Schema(description = "Service included in rate", example = "true")
        private Boolean included;
        
        @Schema(description = "Service active period", example = "During entire stay")
        private String activePeriod;
    }

    /**
     * Deposit information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Deposit details")
    public static class DepositDto {
        
        @Schema(description = "Deposit amount collected", example = "200000.00")
        private BigDecimal amount;
        
        @Schema(description = "Deposit currency", example = "COP")
        private String currency;
        
        @Schema(description = "Deposit payment method", example = "CREDIT_CARD")
        private String paymentMethod;
        
        @Schema(description = "Deposit authorization code", example = "AUTH123456")
        private String authorizationCode;
        
        @Schema(description = "Deposit collection timestamp", example = "2025-11-25T15:28:00")
        private LocalDateTime collectedAt;
        
        @Schema(description = "Refundable deposit", example = "true")
        @Builder.Default
        private Boolean refundable = true;
    }

    /**
     * Key card information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Key card details")
    public static class KeyCardDto {
        
        @Schema(description = "Number of key cards issued", example = "2")
        private Integer cardsIssued;
        
        @Schema(description = "Key card IDs", example = "[\"KEY001\", \"KEY002\"]")
        private List<String> cardIds;
        
        @Schema(description = "Key card expiry date", example = "2025-11-28")
        private java.time.LocalDate expiryDate;
        
        @Schema(description = "Access levels", example = "[\"ROOM\", \"ELEVATOR\", \"POOL\"]")
        private List<String> accessLevels;
        
        @Schema(description = "Programming timestamp", example = "2025-11-25T15:32:00")
        private LocalDateTime programmedAt;
    }

    /**
     * Parking information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Parking assignment details")
    public static class ParkingDto {
        
        @Schema(description = "Parking space assigned", example = "P-A15")
        private String spaceNumber;
        
        @Schema(description = "Vehicle license plate", example = "ABC-123")
        private String licensePlate;
        
        @Schema(description = "Vehicle make and model", example = "Toyota Corolla")
        private String vehicleInfo;
        
        @Schema(description = "Parking fee", example = "10000.00")
        private BigDecimal parkingFee;
        
        @Schema(description = "Parking included in rate", example = "false")
        @Builder.Default
        private Boolean includedInRate = false;
    }

    /**
     * Get total services cost
     */
    public BigDecimal getTotalServicesCost() {
        if (services == null || services.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return services.stream()
                .filter(service -> service.getCost() != null && !service.getIncluded())
                .map(ServiceDto::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Check if guest has special requirements
     */
    public Boolean hasSpecialRequirements() {
        return guest != null && guest.getPreferences() != null && 
               !guest.getPreferences().isEmpty();
    }

    /**
     * Get check-in duration (from reservation time to actual check-in)
     */
    public String getCheckInDuration() {
        // This would typically be calculated from reservation creation time
        return "15 minutes"; // Placeholder
    }

    /**
     * Check if all mandatory steps completed
     */
    public Boolean isCheckInComplete() {
        return documentVerification != null && 
               "VERIFIED".equals(documentVerification.getVerificationStatus()) &&
               policiesAcknowledged != null && policiesAcknowledged &&
               keyCards != null && keyCards.getCardsIssued() > 0;
    }
}