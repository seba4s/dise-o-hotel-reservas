package com.hotel.reservation.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.index.Indexed;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "reservations")
public class Reservation {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String confirmationNumber;
    
    @DBRef
    private User guest;
    
    @DBRef
    private Room room;
    
    @NotNull(message = "Check-in date is required")
    private LocalDate checkInDate;
    
    @NotNull(message = "Check-out date is required")
    private LocalDate checkOutDate;
    
    @NotNull(message = "Number of adults is required")
    @Positive(message = "Number of adults must be positive")
    @Min(value = 1, message = "At least 1 adult is required")
    private Integer adults;
    
    @Min(value = 0, message = "Number of children cannot be negative")
    @Builder.Default
    private Integer children = 0;
    
    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be positive")
    private BigDecimal totalAmount;
    
    @Builder.Default
    private String currency = "COP";
    
    @Builder.Default
    private ReservationStatus status = ReservationStatus.PRE_RESERVATION;
    
    private String specialRequests;
    
    // Guest preferences for this stay
    private GuestPreferences preferences;
    
    // Check-in/out tracking
    private LocalDateTime actualCheckInTime;
    private LocalDateTime actualCheckOutTime;
    private String checkInStaff;
    private String checkOutStaff;
    private CheckInDetails checkInDetails;
    private CheckOutDetails checkOutDetails;
    
    // Pricing breakdown
    private PricingDetails pricing;
    
    // Additional services and charges
    @Builder.Default
    private List<AdditionalCharge> additionalCharges = List.of();
    
    @Builder.Default
    private List<ServiceRequest> serviceRequests = List.of();
    
    // Payment tracking
    private String paymentMethod;
    private String paymentStatus;
    private String paymentTransactionId;
    private List<PaymentTransaction> paymentTransactions;
    
    // Cancellation information
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private String cancelledBy;
    private CancellationPolicy cancellationPolicy;
    private BigDecimal cancellationFee;
    
    // Source and channel tracking
    private String bookingSource; // DIRECT, OTA, PHONE, WALK_IN
    private String channel; // WEBSITE, MOBILE_APP, BOOKING_COM, etc.
    private String userAgent;
    private String sessionId;
    
    // Marketing and promotions
    private String promoCode;
    private BigDecimal discountAmount;
    private String marketingSource;
    
    // Business metadata
    private Map<String, Object> metadata;
    private List<String> tags;
    
    @Indexed
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    /**
     * Reservation status enumeration
     */
    public enum ReservationStatus {
        PRE_RESERVATION("Initial reservation state, pending confirmation"),
        CONFIRMED("Reservation confirmed and paid"),
        CHECKED_IN("Guest has checked in"),
        CHECKED_OUT("Guest has checked out"),
        CANCELLED("Reservation cancelled"),
        NO_SHOW("Guest didn't show up"),
        MODIFIED("Reservation was modified"),
        PENDING_PAYMENT("Waiting for payment completion");
        
        private final String description;
        
        ReservationStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
        
        public boolean isActive() {
            return this == CONFIRMED || this == CHECKED_IN;
        }
        
        public boolean canBeModified() {
            return this == PRE_RESERVATION || this == CONFIRMED;
        }
        
        public boolean canBeCancelled() {
            return this == PRE_RESERVATION || this == CONFIRMED;
        }
        
        public boolean canCheckIn() {
            return this == CONFIRMED;
        }
        
        public boolean canCheckOut() {
            return this == CHECKED_IN;
        }
    }
    
    /**
     * Guest preferences for the stay
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GuestPreferences {
        // Room preferences
        private String roomLocation; // High floor, Low floor, Near elevator
        private String viewPreference; // Ocean, City, Garden
        private Boolean quietRoom;
        private Boolean earlyCheckIn;
        private Boolean lateCheckOut;
        
        // Bed preferences
        private String bedType; // King, Queen, Twin
        private Integer extraPillows;
        private Boolean extraBlankets;
        
        // Services
        private Boolean airportTransfer;
        private Boolean wakeupCall;
        private LocalDateTime wakeupTime;
        private Boolean dailyHousekeeping;
        private Boolean roomService;
        
        // Dietary preferences
        private List<String> dietaryRestrictions;
        private String breakfastPreference;
        
        // Special occasions
        private Boolean specialOccasion;
        private String occasionType; // Birthday, Anniversary, Honeymoon
        private String occasionDetails;
    }
    
    /**
     * Check-in process details
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CheckInDetails {
        private String documentType;
        private String documentNumber;
        private String documentCountry;
        private Boolean documentVerified;
        
        private String emergencyContactName;
        private String emergencyContactPhone;
        
        private String vehicleInfo;
        private String parkingSpace;
        
        private List<String> keyCardsIssued;
        private LocalDateTime keyCardsExpiry;
        
        private BigDecimal depositAmount;
        private String depositPaymentMethod;
        private String depositAuthCode;
        
        private Boolean termsAccepted;
        private Boolean damageWaiverSigned;
        private String staffNotes;
        
        private List<String> servicesRequested;
        private Map<String, String> additionalInfo;
    }
    
    /**
     * Check-out process details
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CheckOutDetails {
        private LocalDateTime requestedCheckOutTime;
        private Boolean lateCheckout;
        private BigDecimal lateCheckoutFee;
        
        private String roomCondition; // EXCELLENT, GOOD, FAIR, DAMAGED
        private List<String> damageReport;
        private List<String> missingItems;
        
        private BigDecimal depositRefund;
        private String depositRefundMethod;
        private String depositRefundReference;
        
        private List<String> keyCardsReturned;
        private Integer missingKeyCards;
        private BigDecimal missingKeyCardFee;
        
        // Guest feedback
        private Integer overallRating; // 1-5
        private Integer roomRating;
        private Integer serviceRating;
        private String feedback;
        private Boolean wouldRecommend;
        
        // Final billing
        private String invoiceNumber;
        private BigDecimal finalAmount;
        private String finalPaymentMethod;
        private String finalTransactionId;
        
        private String staffNotes;
        private List<String> itemsLeftBehind;
    }
    
    /**
     * Detailed pricing breakdown
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PricingDetails {
        private BigDecimal roomRate;
        private Integer nights;
        private BigDecimal roomSubtotal;
        
        private BigDecimal taxRate;
        private BigDecimal taxAmount;
        
        private BigDecimal serviceFee;
        private BigDecimal resortFee;
        private BigDecimal cityTax;
        
        private BigDecimal discountAmount;
        private String discountReason;
        
        private BigDecimal extraGuestFee;
        private BigDecimal extraBedFee;
        
        private BigDecimal totalBeforeExtras;
        private BigDecimal extrasTotal;
        private BigDecimal finalTotal;
        
        // Payment breakdown
        private BigDecimal paidAmount;
        private BigDecimal pendingAmount;
        private BigDecimal refundAmount;
    }
    
    /**
     * Additional charges during stay
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdditionalCharge {
        private String id;
        private String description;
        private BigDecimal amount;
        private String type; // MINIBAR, ROOM_SERVICE, LAUNDRY, etc.
        private Integer quantity;
        private BigDecimal unitPrice;
        private LocalDateTime chargedAt;
        private String chargedBy;
        private Boolean taxable;
        private BigDecimal taxRate;
        private BigDecimal taxAmount;
        private String notes;
        private String receiptNumber;
    }
    
    /**
     * Service requests during stay
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ServiceRequest {
        private String id;
        private String serviceType; // HOUSEKEEPING, MAINTENANCE, CONCIERGE
        private String description;
        private String priority; // LOW, MEDIUM, HIGH, URGENT
        private String status; // REQUESTED, IN_PROGRESS, COMPLETED, CANCELLED
        private LocalDateTime requestedAt;
        private LocalDateTime scheduledFor;
        private LocalDateTime completedAt;
        private String assignedTo;
        private String completedBy;
        private String notes;
        private BigDecimal cost;
    }
    
    /**
     * Payment transaction record
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentTransaction {
        private String id;
        private String transactionId;
        private String type; // PAYMENT, REFUND, AUTHORIZATION, CAPTURE
        private BigDecimal amount;
        private String currency;
        private String method; // CREDIT_CARD, DEBIT_CARD, CASH, BANK_TRANSFER
        private String status; // PENDING, COMPLETED, FAILED, CANCELLED
        private String gatewayResponse;
        private String authorizationCode;
        private LocalDateTime processedAt;
        private String processedBy;
        private String notes;
        private Map<String, String> gatewayMetadata;
    }
    
    /**
     * Cancellation policy information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CancellationPolicy {
        private String policyType; // FLEXIBLE, MODERATE, STRICT, NON_REFUNDABLE
        private Integer freeCancellationHours;
        private LocalDateTime freeCancellationDeadline;
        private BigDecimal cancellationFeePercent;
        private BigDecimal cancellationFeeFixed;
        private String terms;
    }
    
    /**
     * Business methods
     */
    
    public Long getNights() {
        if (checkInDate == null || checkOutDate == null) return null;
        return java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }
    
    public Integer getTotalGuests() {
        return adults + (children != null ? children : 0);
    }
    
    public boolean isActive() {
        return status.isActive();
    }
    
    public boolean canBeModified() {
        return status.canBeModified();
    }
    
    public boolean canBeCancelled() {
        return status.canBeCancelled();
    }
    
    public boolean canCheckIn() {
        return status.canCheckIn() && LocalDate.now().equals(checkInDate);
    }
    
    public boolean canCheckOut() {
        return status.canCheckOut();
    }
    
    public boolean isOverdue() {
        return status == ReservationStatus.CONFIRMED && 
               LocalDate.now().isAfter(checkInDate);
    }
    
    public BigDecimal getTotalAdditionalCharges() {
        if (additionalCharges == null || additionalCharges.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return additionalCharges.stream()
                .map(AdditionalCharge::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public void addAdditionalCharge(AdditionalCharge charge) {
        if (additionalCharges == null) {
            additionalCharges = new java.util.ArrayList<>();
        }
        charge.setId(java.util.UUID.randomUUID().toString());
        charge.setChargedAt(LocalDateTime.now());
        additionalCharges.add(charge);
    }
    
    public void addServiceRequest(ServiceRequest request) {
        if (serviceRequests == null) {
            serviceRequests = new java.util.ArrayList<>();
        }
        request.setId(java.util.UUID.randomUUID().toString());
        request.setRequestedAt(LocalDateTime.now());
        request.setStatus("REQUESTED");
        serviceRequests.add(request);
    }
    
    public void processCheckIn(String staffMember, CheckInDetails details) {
        this.status = ReservationStatus.CHECKED_IN;
        this.actualCheckInTime = LocalDateTime.now();
        this.checkInStaff = staffMember;
        this.checkInDetails = details;
    }
    
    public void processCheckOut(String staffMember, CheckOutDetails details) {
        this.status = ReservationStatus.CHECKED_OUT;
        this.actualCheckOutTime = LocalDateTime.now();
        this.checkOutStaff = staffMember;
        this.checkOutDetails = details;
    }
    
    public void cancel(String reason, String cancelledBy) {
        this.status = ReservationStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
        this.cancelledBy = cancelledBy;
    }
    
    public String getProgressStatus() {
        LocalDate today = LocalDate.now();
        
        switch (status) {
            case CHECKED_OUT:
                return "COMPLETED";
            case CHECKED_IN:
                return "IN_HOUSE";
            case CONFIRMED:
                if (checkInDate.equals(today)) {
                    return "ARRIVING_TODAY";
                } else if (checkOutDate.equals(today)) {
                    return "DEPARTING_TODAY";
                } else if (checkInDate.isBefore(today)) {
                    return "OVERDUE";
                } else {
                    return "CONFIRMED";
                }
            default:
                return status.toString();
        }
    }
    
    public BigDecimal calculateCancellationFee() {
        if (cancellationPolicy == null) return BigDecimal.ZERO;
        
        LocalDateTime now = LocalDateTime.now();
        if (cancellationPolicy.getFreeCancellationDeadline() != null &&
            now.isBefore(cancellationPolicy.getFreeCancellationDeadline())) {
            return BigDecimal.ZERO;
        }
        
        if (cancellationPolicy.getCancellationFeeFixed() != null) {
            return cancellationPolicy.getCancellationFeeFixed();
        }
        
        if (cancellationPolicy.getCancellationFeePercent() != null) {
            return totalAmount.multiply(cancellationPolicy.getCancellationFeePercent())
                             .divide(BigDecimal.valueOf(100));
        }
        
        return BigDecimal.ZERO;
    }
}