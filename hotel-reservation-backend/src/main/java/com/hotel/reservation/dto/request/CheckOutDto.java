package com.hotel.reservation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
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
@Schema(description = "Check-out process request data - HU010")
public class CheckOutDto {

    @Schema(description = "Reservation ID or confirmation number", 
            example = "CONF73829456", required = true)
    @NotBlank(message = "Reservation ID is required")
    private String reservationId;

    @Schema(description = "Actual check-out time", example = "2025-11-28T11:30:00", required = false)
    private LocalDateTime actualCheckOutTime;

    @Schema(description = "Room inspection status", example = "GOOD", required = false,
            allowableValues = {"EXCELLENT", "GOOD", "FAIR", "NEEDS_ATTENTION", "DAMAGED"})
    @Builder.Default
    private String roomCondition = "GOOD";

    @Schema(description = "Additional charges during stay", required = false)
    @Valid
    private List<AdditionalChargeDto> additionalCharges;

    @Schema(description = "Deposit refund amount", example = "100.00", required = false)
    @DecimalMin(value = "0.0", message = "Deposit refund cannot be negative")
    private BigDecimal depositRefund;

    @Schema(description = "Payment method for additional charges", 
            example = "CREDIT_CARD", required = false,
            allowableValues = {"CREDIT_CARD", "DEBIT_CARD", "CASH", "ROOM_CHARGE"})
    private String paymentMethod;

    @Schema(description = "Guest satisfaction rating (1-5)", example = "5", required = false)
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer guestRating;

    @Schema(description = "Guest feedback or comments", 
            example = "Excellent service, very clean room", required = false)
    @Size(max = 1000, message = "Guest feedback must not exceed 1000 characters")
    private String guestFeedback;

    @Schema(description = "Check-out notes from staff", 
            example = "Late checkout approved, no issues found", required = false)
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    @Schema(description = "Key cards returned count", example = "2", required = false)
    private Integer keyCardsReturned;

    @Schema(description = "Items left in room", 
            example = "Charger cable in drawer", required = false)
    @Size(max = 200, message = "Lost items description must not exceed 200 characters")
    private String itemsLeftBehind;

    @Schema(description = "Housekeeping priority", example = "STANDARD", required = false,
            allowableValues = {"LOW", "STANDARD", "HIGH", "URGENT"})
    @Builder.Default
    private String housekeepingPriority = "STANDARD";

    @Schema(description = "Maintenance issues reported", 
            example = "Air conditioning making noise", required = false)
    @Size(max = 500, message = "Maintenance issues must not exceed 500 characters")
    private String maintenanceIssues;

    @Schema(description = "Guest opted for email receipt", example = "true", required = false)
    @Builder.Default
    private Boolean emailReceipt = true;

    @Schema(description = "Print receipt at front desk", example = "false", required = false)
    @Builder.Default
    private Boolean printReceipt = false;

    /**
     * Additional charge item
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Additional charge item")
    public static class AdditionalChargeDto {

        @Schema(description = "Charge description", example = "Minibar consumption", required = true)
        @NotBlank(message = "Charge description is required")
        @Size(max = 100, message = "Description must not exceed 100 characters")
        private String description;

        @Schema(description = "Charge amount", example = "25.50", required = true)
        @NotNull(message = "Charge amount is required")
        @DecimalMin(value = "0.01", message = "Charge amount must be greater than 0")
        @DecimalMax(value = "9999.99", message = "Charge amount cannot exceed 9999.99")
        private BigDecimal amount;

        @Schema(description = "Charge type/category", example = "MINIBAR", required = true,
                allowableValues = {"MINIBAR", "ROOM_SERVICE", "LAUNDRY", "TELEPHONE", 
                                 "PARKING", "LATE_CHECKOUT", "DAMAGES", "OTHER"})
        @NotBlank(message = "Charge type is required")
        private String type;

        @Schema(description = "Quantity of items", example = "2", required = false)
        @Min(value = 1, message = "Quantity must be at least 1")
        @Builder.Default
        private Integer quantity = 1;

        @Schema(description = "Unit price", example = "12.75", required = false)
        @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
        private BigDecimal unitPrice;

        @Schema(description = "Tax rate applied", example = "0.19", required = false)
        @DecimalMin(value = "0.0", message = "Tax rate cannot be negative")
        @DecimalMax(value = "1.0", message = "Tax rate cannot exceed 100%")
        private BigDecimal taxRate;

        @Schema(description = "Date and time when charge was incurred", 
                example = "2025-11-27T20:30:00", required = false)
        private LocalDateTime chargeDateTime;

        /**
         * Calculate total amount including tax
         */
        public BigDecimal getTotalAmount() {
            if (amount == null) return BigDecimal.ZERO;
            
            if (taxRate != null && taxRate.compareTo(BigDecimal.ZERO) > 0) {
                return amount.add(amount.multiply(taxRate));
            }
            
            return amount;
        }

        /**
         * Calculate amount based on quantity and unit price
         */
        public BigDecimal calculateAmount() {
            if (unitPrice == null || quantity == null) return amount;
            
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    /**
     * Calculate total additional charges
     */
    public BigDecimal getTotalAdditionalCharges() {
        if (additionalCharges == null || additionalCharges.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        return additionalCharges.stream()
                .map(AdditionalChargeDto::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Check if check-out has any issues
     */
    public boolean hasIssues() {
        return (maintenanceIssues != null && !maintenanceIssues.trim().isEmpty()) ||
               (itemsLeftBehind != null && !itemsLeftBehind.trim().isEmpty()) ||
               "NEEDS_ATTENTION".equals(roomCondition) || 
               "DAMAGED".equals(roomCondition);
    }

    /**
     * Check if guest provided feedback
     */
    public boolean hasFeedback() {
        return (guestRating != null && guestRating > 0) ||
               (guestFeedback != null && !guestFeedback.trim().isEmpty());
    }

    /**
     * Check if additional charges exist
     */
    public boolean hasAdditionalCharges() {
        return additionalCharges != null && !additionalCharges.isEmpty();
    }
}