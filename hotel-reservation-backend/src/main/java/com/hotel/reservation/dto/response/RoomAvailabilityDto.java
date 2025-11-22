package com.hotel.reservation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Room availability information - HU001")
public class RoomAvailabilityDto {

    @Schema(description = "Room unique identifier", example = "64abc123def456789012", required = true)
    private String id;

    @Schema(description = "Room number", example = "201", required = true)
    private String roomNumber;

    @Schema(description = "Room type", example = "Deluxe Vista al Mar", required = true)
    private String roomType;

    @Schema(description = "Room capacity (max guests)", example = "4", required = true)
    private Integer capacity;

    @Schema(description = "Room size in square meters", example = "45", required = false)
    private Integer size;

    @Schema(description = "Bed configuration", example = "King Size", required = false)
    private String bedType;

    @Schema(description = "Base price per night", example = "500000.00", required = true)
    private BigDecimal basePrice;

    @Schema(description = "Total price for the entire stay", example = "1500000.00", required = true)
    private BigDecimal totalPrice;

    @Schema(description = "Price per night for the selected dates", example = "500000.00", required = true)
    private BigDecimal pricePerNight;

    @Schema(description = "Currency code", example = "COP", required = true)
    @Builder.Default
    private String currency = "COP";

    @Schema(description = "Room description", 
            example = "Spacious room with ocean view and modern amenities", required = false)
    private String description;

    @Schema(description = "Room amenities", 
            example = "[\"WiFi\", \"Air Conditioning\", \"Ocean View\", \"Minibar\"]", required = false)
    private List<String> amenities;

    @Schema(description = "Room photos URLs", 
            example = "[\"https://hotel.com/photos/room1.jpg\", \"https://hotel.com/photos/room2.jpg\"]", 
            required = false)
    private List<String> photos;

    @Schema(description = "Room availability status", example = "true", required = true)
    private Boolean available;

    @Schema(description = "Check-in date for pricing", example = "2025-11-25", required = false)
    private LocalDate checkInDate;

    @Schema(description = "Check-out date for pricing", example = "2025-11-28", required = false)
    private LocalDate checkOutDate;

    @Schema(description = "Number of nights", example = "3", required = false)
    private Long nights;

    @Schema(description = "Special offers or discounts", required = false)
    private List<OfferDto> offers;

    @Schema(description = "Rate plan information", required = false)
    private RatePlanDto ratePlan;

    @Schema(description = "Cancellation policy", required = false)
    private CancellationPolicyDto cancellationPolicy;

    @Schema(description = "Last availability check", example = "2025-11-22T03:51:37", required = false)
    private java.time.LocalDateTime lastChecked;

    @Schema(description = "Immediate booking availability", example = "true", required = false)
    @Builder.Default
    private Boolean instantBooking = true;

    /**
     * Special offer information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Special offer details")
    public static class OfferDto {
        
        @Schema(description = "Offer name", example = "Early Bird Discount")
        private String name;
        
        @Schema(description = "Discount percentage", example = "15")
        private Integer discountPercent;
        
        @Schema(description = "Discount amount", example = "75000.00")
        private BigDecimal discountAmount;
        
        @Schema(description = "Offer description", example = "Book 30 days in advance and save 15%")
        private String description;
        
        @Schema(description = "Offer valid until", example = "2025-12-31")
        private LocalDate validUntil;
    }

    /**
     * Rate plan information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Rate plan details")
    public static class RatePlanDto {
        
        @Schema(description = "Plan name", example = "Flexible Rate")
        private String name;
        
        @Schema(description = "Includes breakfast", example = "true")
        private Boolean includesBreakfast;
        
        @Schema(description = "Free cancellation", example = "true")
        private Boolean freeCancellation;
        
        @Schema(description = "Prepayment required", example = "false")
        private Boolean prepaymentRequired;
        
        @Schema(description = "Plan benefits", example = "[\"Free WiFi\", \"Late checkout\"]")
        private List<String> benefits;
    }

    /**
     * Cancellation policy information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Cancellation policy details")
    public static class CancellationPolicyDto {
        
        @Schema(description = "Policy type", example = "FLEXIBLE")
        private String type;
        
        @Schema(description = "Free cancellation until", example = "2025-11-23T15:00:00")
        private java.time.LocalDateTime freeCancellationUntil;
        
        @Schema(description = "Cancellation penalty percentage", example = "10")
        private Integer penaltyPercent;
        
        @Schema(description = "Policy description", 
                example = "Free cancellation until 24 hours before check-in")
        private String description;
    }

    /**
     * Calculate price per person
     */
    public BigDecimal getPricePerPerson(Integer guests) {
        if (guests == null || guests <= 0) return totalPrice;
        return totalPrice.divide(BigDecimal.valueOf(guests), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Check if room has specific amenity
     */
    public Boolean hasAmenity(String amenity) {
        return amenities != null && amenities.contains(amenity);
    }

    /**
     * Get savings amount if there are offers
     */
    public BigDecimal getSavingsAmount() {
        if (offers == null || offers.isEmpty()) return BigDecimal.ZERO;
        
        return offers.stream()
                .map(OfferDto::getDiscountAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Check if breakfast is included
     */
    public Boolean includesBreakfast() {
        return ratePlan != null && ratePlan.getIncludesBreakfast() != null && 
               ratePlan.getIncludesBreakfast();
    }
}