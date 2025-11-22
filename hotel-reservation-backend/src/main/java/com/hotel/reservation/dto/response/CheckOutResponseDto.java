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
@Schema(description = "Check-out process response - HU010")
public class CheckOutResponseDto {

    @Schema(description = "Check-out process ID", example = "checkout_64abc123", required = true)
    private String checkOutId;

    @Schema(description = "Reservation ID", example = "64abc123def456789012", required = true)
    private String reservationId;

    @Schema(description = "Confirmation number", example = "CONF73829456", required = true)
    private String confirmationNumber;

    @Schema(description = "Guest information", required = true)
    private GuestDto guest;

    @Schema(description = "Room information", required = true)
    private RoomDto room;

    @Schema(description = "Check-out timestamp", example = "2025-11-28T11:00:00", required = true)
    private LocalDateTime checkOutTime;

    @Schema(description = "Staff member who processed check-out", example = "seba4s", required = true)
    private String processedBy;

    @Schema(description = "Stay summary", required = true)
    private StaySummaryDto staySummary;

    @Schema(description = "Final billing information", required = true)
    private BillingDto billing;

    @Schema(description = "Additional charges during stay", required = false)
    private List<AdditionalChargeDto> additionalCharges;

    @Schema(description = "Deposit information and refund", required = false)
    private DepositRefundDto depositRefund;

    @Schema(description = "Room inspection results", required = true)
    private RoomInspectionDto roomInspection;

    @Schema(description = "Key cards returned", required = true)
    private KeyCardReturnDto keyCardReturn;

    @Schema(description = "Final invoice details", required = true)
    private InvoiceDto invoice;

    @Schema(description = "Guest feedback", required = false)
    private GuestFeedbackDto guestFeedback;

    @Schema(description = "Lost and found items", required = false)
    private List<LostItemDto> lostItems;

    @Schema(description = "Final amount charged", example = "1625000.00", required = true)
    private BigDecimal finalAmount;

    @Schema(description = "Currency", example = "COP", required = true)
    @Builder.Default
    private String currency = "COP";

    @Schema(description = "Check-out status", example = "COMPLETED", required = true)
    @Builder.Default
    private String status = "COMPLETED";

    @Schema(description = "Check-out notes from staff", 
            example = "Smooth checkout, guest satisfied", required = false)
    private String notes;

    /**
     * Guest summary for check-out
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Guest check-out summary")
    public static class GuestDto {
        
        @Schema(description = "Guest full name", example = "Ana López")
        private String fullName;
        
        @Schema(description = "Guest email", example = "ana.lopez@example.com")
        private String email;
        
        @Schema(description = "Room number stayed", example = "201")
        private String roomNumber;
        
        @Schema(description = "Total nights stayed", example = "3")
        private Integer nightsStayed;
        
        @Schema(description = "Guest satisfaction level", example = "EXCELLENT")
        private String satisfactionLevel;
    }

    /**
     * Room summary
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Room details for check-out")
    public static class RoomDto {
        
        @Schema(description = "Room number", example = "201")
        private String roomNumber;
        
        @Schema(description = "Room type", example = "Deluxe Vista al Mar")
        private String roomType;
        
        @Schema(description = "Current room status after check-out", example = "NEEDS_CLEANING")
        private String currentStatus;
        
        @Schema(description = "Next guest check-in", example = "2025-11-29T15:00:00")
        private LocalDateTime nextCheckIn;
        
        @Schema(description = "Housekeeping priority", example = "STANDARD")
        private String housekeepingPriority;
    }

    /**
     * Stay summary
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Guest stay summary")
    public static class StaySummaryDto {
        
        @Schema(description = "Check-in date and time", example = "2025-11-25T15:30:00")
        private LocalDateTime actualCheckIn;
        
        @Schema(description = "Check-out date and time", example = "2025-11-28T11:00:00")
        private LocalDateTime actualCheckOut;
        
        @Schema(description = "Total stay duration", example = "2 days, 19 hours, 30 minutes")
        private String stayDuration;
        
        @Schema(description = "Number of nights", example = "3")
        private Integer nightsStayed;
        
        @Schema(description = "Services used during stay", 
                example = "[\"Room Service\", \"Laundry\", \"Minibar\"]")
        private List<String> servicesUsed;
        
        @Schema(description = "Late checkout applied", example = "false")
        @Builder.Default
        private Boolean lateCheckout = false;
        
        @Schema(description = "Early checkin was applied", example = "true")
        @Builder.Default
        private Boolean earlyCheckin = false;
    }

    /**
     * Billing information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Final billing details")
    public static class BillingDto {
        
        @Schema(description = "Room charges subtotal", example = "1500000.00")
        private BigDecimal roomCharges;
        
        @Schema(description = "Additional services charges", example = "75000.00")
        private BigDecimal servicesCharges;
        
        @Schema(description = "Taxes applied", example = "285000.00")
        private BigDecimal taxes;
        
        @Schema(description = "Service fees", example = "50000.00")
        private BigDecimal serviceFees;
        
        @Schema(description = "Total amount before deposit", example = "1910000.00")
        private BigDecimal totalBeforeDeposit;
        
        @Schema(description = "Deposit used", example = "200000.00")
        private BigDecimal depositUsed;
        
        @Schema(description = "Additional payment required", example = "1710000.00")
        private BigDecimal additionalPayment;
        
        @Schema(description = "Payment method for balance", example = "CREDIT_CARD")
        private String paymentMethod;
        
        @Schema(description = "Transaction ID for final payment", example = "txn_final_12345")
        private String transactionId;
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
        
        @Schema(description = "Charge date", example = "2025-11-27")
        private java.time.LocalDate chargeDate;
        
        @Schema(description = "Charge description", example = "Minibar - Soft drinks")
        private String description;
        
        @Schema(description = "Charge amount", example = "15000.00")
        private BigDecimal amount;
        
        @Schema(description = "Charge category", example = "MINIBAR")
        private String category;
        
        @Schema(description = "Quantity", example = "3")
        private Integer quantity;
        
        @Schema(description = "Unit price", example = "5000.00")
        private BigDecimal unitPrice;
    }

    /**
     * Deposit refund information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Deposit refund details")
    public static class DepositRefundDto {
        
        @Schema(description = "Original deposit amount", example = "200000.00")
        private BigDecimal originalDeposit;
        
        @Schema(description = "Amount used for charges", example = "25000.00")
        private BigDecimal amountUsed;
        
        @Schema(description = "Refund amount", example = "175000.00")
        private BigDecimal refundAmount;
        
        @Schema(description = "Refund method", example = "ORIGINAL_PAYMENT_METHOD")
        private String refundMethod;
        
        @Schema(description = "Refund status", example = "PROCESSED")
        private String refundStatus;
        
        @Schema(description = "Refund processing time", example = "3-5 business days")
        private String processingTime;
        
        @Schema(description = "Refund reference", example = "REF_12345ABC")
        private String refundReference;
    }

    /**
     * Room inspection results
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Room inspection details")
    public static class RoomInspectionDto {
        
        @Schema(description = "Overall room condition", example = "GOOD")
        private String overallCondition;
        
        @Schema(description = "Cleanliness rating", example = "EXCELLENT")
        private String cleanliness;
        
        @Schema(description = "Damage assessment", example = "NO_DAMAGE")
        private String damageAssessment;
        
        @Schema(description = "Maintenance issues found", 
                example = "[\"Air conditioning filter needs replacement\"]")
        private List<String> maintenanceIssues;
        
        @Schema(description = "Missing items", example = "[]")
        private List<String> missingItems;
        
        @Schema(description = "Housekeeping notes", 
                example = "Room left in excellent condition")
        private String housekeepingNotes;
        
        @Schema(description = "Inspection completed by", example = "housekeeping_staff")
        private String inspectedBy;
        
        @Schema(description = "Inspection timestamp", example = "2025-11-28T11:15:00")
        private LocalDateTime inspectionTime;
    }

    /**
     * Key card return information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Key card return details")
    public static class KeyCardReturnDto {
        
        @Schema(description = "Number of cards issued", example = "2")
        private Integer cardsIssued;
        
        @Schema(description = "Number of cards returned", example = "2")
        private Integer cardsReturned;
        
        @Schema(description = "Missing cards", example = "0")
        private Integer missingCards;
        
        @Schema(description = "Card IDs returned", example = "[\"KEY001\", \"KEY002\"]")
        private List<String> returnedCardIds;
        
        @Schema(description = "Missing card fee", example = "0.00")
        private BigDecimal missingCardFee;
        
        @Schema(description = "Cards deactivated", example = "true")
        @Builder.Default
        private Boolean cardsDeactivated = true;
    }

    /**
     * Invoice information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Invoice details")
    public static class InvoiceDto {
        
        @Schema(description = "Invoice number", example = "INV-2025-001234")
        private String invoiceNumber;
        
        @Schema(description = "Invoice date", example = "2025-11-28T11:00:00")
        private LocalDateTime invoiceDate;
        
        @Schema(description = "Invoice URL for download", 
                example = "https://hotel.com/invoices/INV-2025-001234.pdf")
        private String invoiceUrl;
        
        @Schema(description = "Email sent to guest", example = "true")
        @Builder.Default
        private Boolean emailSent = true;
        
        @Schema(description = "Print provided to guest", example = "false")
        @Builder.Default
        private Boolean printProvided = false;
    }

    /**
     * Guest feedback
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Guest feedback details")
    public static class GuestFeedbackDto {
        
        @Schema(description = "Overall rating (1-5)", example = "5")
        private Integer overallRating;
        
        @Schema(description = "Room rating (1-5)", example = "5")
        private Integer roomRating;
        
        @Schema(description = "Service rating (1-5)", example = "4")
        private Integer serviceRating;
        
        @Schema(description = "Value rating (1-5)", example = "4")
        private Integer valueRating;
        
        @Schema(description = "Written comments", 
                example = "Excellent stay, room was clean and comfortable")
        private String comments;
        
        @Schema(description = "Would recommend hotel", example = "true")
        private Boolean wouldRecommend;
        
        @Schema(description = "Likelihood to return (1-5)", example = "5")
        private Integer likelihoodToReturn;
        
        @Schema(description = "Feedback provided timestamp", example = "2025-11-28T10:55:00")
        private LocalDateTime providedAt;
    }

    /**
     * Lost item information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Lost and found item details")
    public static class LostItemDto {
        
        @Schema(description = "Item description", example = "iPhone charger cable")
        private String description;
        
        @Schema(description = "Location found", example = "Bedside table drawer")
        private String locationFound;
        
        @Schema(description = "Found by staff member", example = "housekeeping_staff")
        private String foundBy;
        
        @Schema(description = "Found timestamp", example = "2025-11-28T11:10:00")
        private LocalDateTime foundAt;
        
        @Schema(description = "Guest contacted about item", example = "true")
        @Builder.Default
        private Boolean guestContacted = false;
        
        @Schema(description = "Item claimed by guest", example = "false")
        @Builder.Default
        private Boolean claimed = false;
    }

    /**
     * Calculate total stay cost
     */
    public BigDecimal getTotalStayCost() {
        BigDecimal total = billing != null ? billing.getRoomCharges() : BigDecimal.ZERO;
        
        if (additionalCharges != null && !additionalCharges.isEmpty()) {
            BigDecimal additionalTotal = additionalCharges.stream()
                    .map(AdditionalChargeDto::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            total = total.add(additionalTotal);
        }
        
        return total;
    }

    /**
     * Check if guest left any feedback
     */
    public Boolean hasGuestFeedback() {
        return guestFeedback != null && 
               (guestFeedback.getOverallRating() != null || 
                guestFeedback.getComments() != null);
    }

    /**
     * Check if there were any issues during stay
     */
    public Boolean hadIssuesDuringStay() {
        return (roomInspection != null && roomInspection.getMaintenanceIssues() != null && 
                !roomInspection.getMaintenanceIssues().isEmpty()) ||
               (keyCardReturn != null && keyCardReturn.getMissingCards() > 0) ||
               (lostItems != null && !lostItems.isEmpty());
    }

    /**
     * Get guest satisfaction summary
     */
    public String getGuestSatisfactionSummary() {
        if (guestFeedback == null || guestFeedback.getOverallRating() == null) {
            return "No feedback provided";
        }
        
        int rating = guestFeedback.getOverallRating();
        if (rating >= 5) return "Excellent";
        else if (rating >= 4) return "Very Good";
        else if (rating >= 3) return "Good";
        else if (rating >= 2) return "Fair";
        else return "Poor";
    }
}