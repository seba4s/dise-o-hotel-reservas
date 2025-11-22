package com.hotel.reservation.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "rooms")
public class Room {
    
    @Id
    private String id;
    
    @NotBlank(message = "Room number is required")
    @Indexed(unique = true)
    private String roomNumber;
    
    @NotBlank(message = "Room type is required")
    private String roomType; // Standard, Deluxe, Suite, etc.
    
    @NotNull(message = "Capacity is required")
    @Positive(message = "Capacity must be positive")
    @Min(value = 1, message = "Room must accommodate at least 1 person")
    @Max(value = 10, message = "Room cannot accommodate more than 10 people")
    private Integer capacity;
    
    @Min(value = 15, message = "Room size must be at least 15 square meters")
    @Max(value = 200, message = "Room size cannot exceed 200 square meters")
    private Integer size; // in square meters
    
    private String bedType; // Single, Double, Queen, King, Twin
    
    @NotNull(message = "Base price is required")
    @Positive(message = "Base price must be positive")
    private BigDecimal basePrice;
    
    private String description;
    
    @Builder.Default
    private List<String> amenities = List.of();
    
    @Builder.Default
    private List<String> photos = List.of();
    
    @Builder.Default
    private RoomStatus status = RoomStatus.AVAILABLE;
    
    // Room physical details
    private Integer floor;
    private String view; // Ocean, City, Garden, etc.
    private Boolean smokingAllowed;
    private Boolean petFriendly;
    private Boolean accessible; // Wheelchair accessible
    
    // Room features
    private Boolean hasBalcony;
    private Boolean hasKitchenette;
    private Boolean hasWorkDesk;
    private Boolean hasSafe;
    private Boolean hasMinibar;
    
    // Technical specifications
    private String wifiNetwork;
    private String wifiPassword;
    private String hvacControlType; // Central, Individual
    private List<String> electricalOutlets; // USB, European, American
    
    // Maintenance information
    private LocalDateTime lastMaintenance;
    private LocalDateTime nextScheduledMaintenance;
    private List<MaintenanceRecord> maintenanceHistory;
    
    // Pricing and inventory
    private RoomInventory inventory;
    private List<SeasonalPricing> seasonalPricing;
    
    // Business rules
    private RoomRules rules;
    
    @Builder.Default
    private Boolean active = true;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    /**
     * Room status enumeration
     */
    public enum RoomStatus {
        AVAILABLE("Ready for guest occupancy"),
        OCCUPIED("Currently occupied by guest"),
        MAINTENANCE("Under maintenance - not bookable"),
        CLEANING("Being cleaned by housekeeping"),
        OUT_OF_ORDER("Temporarily out of service"),
        RESERVED("Reserved but not yet occupied"),
        BLOCKED("Administratively blocked");
        
        private final String description;
        
        RoomStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
        
        public boolean isBookable() {
            return this == AVAILABLE;
        }
        
        public boolean requiresHousekeeping() {
            return this == CLEANING || this == MAINTENANCE;
        }
    }
    
    /**
     * Maintenance record for tracking room maintenance
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MaintenanceRecord {
        private String id;
        private String type; // PREVENTIVE, CORRECTIVE, EMERGENCY
        private String description;
        private LocalDateTime scheduledDate;
        private LocalDateTime completedDate;
        private String performedBy;
        private BigDecimal cost;
        private String notes;
        private List<String> partsReplaced;
        private String priority; // LOW, MEDIUM, HIGH, CRITICAL
    }
    
    /**
     * Room inventory tracking
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoomInventory {
        private Map<String, Integer> amenityStock; // towels, pillows, etc.
        private LocalDateTime lastInventoryCheck;
        private String lastCheckedBy;
        private List<String> missingItems;
        private List<String> damagedItems;
    }
    
    /**
     * Seasonal pricing configuration
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SeasonalPricing {
        private String seasonName; // High Season, Low Season, Holiday
        private java.time.LocalDate startDate;
        private java.time.LocalDate endDate;
        private BigDecimal priceModifier; // Multiplier or fixed amount
        private String modifierType; // PERCENTAGE, FIXED_AMOUNT
        private String description;
        private List<java.time.DayOfWeek> applicableDays;
    }
    
    /**
     * Room rules and restrictions
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoomRules {
        @Builder.Default
        private Integer maxOccupancy = 2;
        
        @Builder.Default
        private Integer maxChildren = 2;
        
        @Builder.Default
        private Boolean allowExtraBed = true;
        
        @Builder.Default
        private BigDecimal extraBedCharge = BigDecimal.ZERO;
        
        @Builder.Default
        private Boolean allowEarlyCheckIn = true;
        
        @Builder.Default
        private Boolean allowLateCheckOut = true;
        
        @Builder.Default
        private BigDecimal lateCheckOutCharge = BigDecimal.valueOf(50000);
        
        @Builder.Default
        private Integer cancellationHours = 24;
        
        @Builder.Default
        private Boolean refundable = true;
        
        private String specialInstructions;
    }
    
    /**
     * Business methods
     */
    
    public boolean isAvailable() {
        return status == RoomStatus.AVAILABLE && active;
    }
    
    public boolean isOccupied() {
        return status == RoomStatus.OCCUPIED;
    }
    
    public boolean needsMaintenance() {
        return status == RoomStatus.MAINTENANCE || status == RoomStatus.OUT_OF_ORDER;
    }
    
    public boolean canAccommodate(Integer guests) {
        Integer maxCapacity = rules != null ? rules.getMaxOccupancy() : capacity;
        return guests <= maxCapacity;
    }
    
    public BigDecimal calculatePrice(java.time.LocalDate date) {
        BigDecimal price = basePrice;
        
        if (seasonalPricing != null) {
            for (SeasonalPricing seasonal : seasonalPricing) {
                if (isDateInSeason(date, seasonal)) {
                    if ("PERCENTAGE".equals(seasonal.getModifierType())) {
                        price = price.multiply(seasonal.getPriceModifier());
                    } else if ("FIXED_AMOUNT".equals(seasonal.getModifierType())) {
                        price = price.add(seasonal.getPriceModifier());
                    }
                    break;
                }
            }
        }
        
        return price;
    }
    
    private boolean isDateInSeason(java.time.LocalDate date, SeasonalPricing seasonal) {
        return !date.isBefore(seasonal.getStartDate()) && !date.isAfter(seasonal.getEndDate());
    }
    
    public boolean hasAmenity(String amenity) {
        return amenities != null && amenities.contains(amenity);
    }
    
    public void addMaintenanceRecord(MaintenanceRecord record) {
        if (maintenanceHistory == null) {
            maintenanceHistory = new java.util.ArrayList<>();
        }
        maintenanceHistory.add(record);
        updateLastMaintenance(record.getCompletedDate());
    }
    
    private void updateLastMaintenance(LocalDateTime date) {
        if (date != null) {
            this.lastMaintenance = date;
        }
    }
    
    public void setOutOfOrder(String reason) {
        this.status = RoomStatus.OUT_OF_ORDER;
        // Could add a maintenance record here
    }
    
    public void markAsAvailable() {
        this.status = RoomStatus.AVAILABLE;
    }
    
    public void markAsOccupied() {
        this.status = RoomStatus.OCCUPIED;
    }
    
    public void markForCleaning() {
        this.status = RoomStatus.CLEANING;
    }
    
    public Integer getFloorFromRoomNumber() {
        if (roomNumber != null && roomNumber.length() > 0) {
            try {
                return Integer.parseInt(roomNumber.substring(0, 1));
            } catch (NumberFormatException e) {
                return 1; // Default floor
            }
        }
        return floor != null ? floor : 1;
    }
    
    public boolean requiresMaintenanceSoon() {
        if (nextScheduledMaintenance == null) return false;
        return nextScheduledMaintenance.isBefore(LocalDateTime.now().plusDays(7));
    }
    
    public String getRoomCategory() {
        // Categorize based on price and features
        if (basePrice.compareTo(BigDecimal.valueOf(800000)) > 0) {
            return "LUXURY";
        } else if (basePrice.compareTo(BigDecimal.valueOf(500000)) > 0) {
            return "PREMIUM";
        } else if (basePrice.compareTo(BigDecimal.valueOf(300000)) > 0) {
            return "STANDARD";
        } else {
            return "ECONOMY";
        }
    }
}