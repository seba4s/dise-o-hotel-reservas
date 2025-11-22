package com.hotel.reservation.mapper;

import com.hotel.reservation.dto.response.RoomAvailabilityDto;
import com.hotel.reservation.model.Room;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * MapStruct mapper for Room entity and availability DTOs
 * Handles conversions for HU001 (Availability Search) responses
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {DateTimeMapper.class}
)
public interface RoomMapper {

    /**
     * Convert Room entity to basic RoomAvailabilityDto
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "roomNumber", source = "roomNumber")
    @Mapping(target = "roomType", source = "roomType")
    @Mapping(target = "capacity", source = "capacity")
    @Mapping(target = "size", source = "size")
    @Mapping(target = "bedType", source = "bedType")
    @Mapping(target = "basePrice", source = "basePrice")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "amenities", source = "amenities")
    @Mapping(target = "photos", source = "photos")
    @Mapping(target = "currency", constant = "COP")
    @Mapping(target = "available", constant = "true") // Will be set based on availability check
    @Mapping(target = "totalPrice", ignore = true) // Calculated based on dates
    @Mapping(target = "pricePerNight", source = "basePrice")
    @Mapping(target = "checkInDate", ignore = true) // Set from search parameters
    @Mapping(target = "checkOutDate", ignore = true) // Set from search parameters
    @Mapping(target = "nights", ignore = true) // Calculated
    @Mapping(target = "offers", ignore = true) // Set based on business rules
    @Mapping(target = "ratePlan", ignore = true) // Set based on business rules
    @Mapping(target = "cancellationPolicy", ignore = true) // Set based on business rules
    @Mapping(target = "lastChecked", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "instantBooking", constant = "true")
    RoomAvailabilityDto toAvailabilityDto(Room room);

    /**
     * Convert Room entity to RoomAvailabilityDto with pricing calculation
     */
    @Mapping(target = "id", source = "room.id")
    @Mapping(target = "roomNumber", source = "room.roomNumber")
    @Mapping(target = "roomType", source = "room.roomType")
    @Mapping(target = "capacity", source = "room.capacity")
    @Mapping(target = "size", source = "room.size")
    @Mapping(target = "bedType", source = "room.bedType")
    @Mapping(target = "basePrice", source = "room.basePrice")
    @Mapping(target = "description", source = "room.description")
    @Mapping(target = "amenities", source = "room.amenities")
    @Mapping(target = "photos", source = "room.photos")
    @Mapping(target = "currency", constant = "COP")
    @Mapping(target = "checkInDate", source = "checkInDate")
    @Mapping(target = "checkOutDate", source = "checkOutDate")
    @Mapping(target = "available", source = "available")
    @Mapping(target = "pricePerNight", source = "room.basePrice")
    @Mapping(target = "totalPrice", ignore = true) // Will be calculated in @AfterMapping
    @Mapping(target = "nights", ignore = true) // Will be calculated in @AfterMapping
    @Mapping(target = "offers", ignore = true)
    @Mapping(target = "ratePlan", ignore = true)
    @Mapping(target = "cancellationPolicy", ignore = true)
    @Mapping(target = "lastChecked", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "instantBooking", constant = "true")
    RoomAvailabilityDto toAvailabilityDtoWithDates(Room room, 
                                                   LocalDate checkInDate, 
                                                   LocalDate checkOutDate, 
                                                   Boolean available);

    /**
     * Convert Room entity to ReservationResponseDto.RoomDto
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "roomNumber", source = "roomNumber")
    @Mapping(target = "roomType", source = "roomType")
    @Mapping(target = "capacity", source = "capacity")
    @Mapping(target = "bedType", source = "bedType")
    @Mapping(target = "basePrice", source = "basePrice")
    @Mapping(target = "amenities", source = "amenities")
    com.hotel.reservation.dto.response.ReservationResponseDto.RoomDto toReservationRoomDto(Room room);

    /**
     * Convert Room entity to CheckInResponseDto.RoomAssignmentDto
     */
    @Mapping(target = "roomNumber", source = "roomNumber")
    @Mapping(target = "roomType", source = "roomType")
    @Mapping(target = "floor", ignore = true) // Extract from room number or set separately
    @Mapping(target = "features", source = "amenities")
    @Mapping(target = "wifiNetwork", ignore = true) // Generated separately
    @Mapping(target = "wifiPassword", ignore = true) // Generated separately
    @Mapping(target = "roomRate", source = "basePrice")
    com.hotel.reservation.dto.response.CheckInResponseDto.RoomAssignmentDto toCheckInRoomDto(Room room);

    /**
     * Convert Room entity to CheckOutResponseDto.RoomDto
     */
    @Mapping(target = "roomNumber", source = "roomNumber")
    @Mapping(target = "roomType", source = "roomType")
    @Mapping(target = "currentStatus", ignore = true) // Set based on room status after checkout
    @Mapping(target = "nextCheckIn", ignore = true) // Set based on next reservation
    @Mapping(target = "housekeepingPriority", ignore = true) // Set based on business rules
    com.hotel.reservation.dto.response.CheckOutResponseDto.RoomDto toCheckOutRoomDto(Room room);

    /**
     * After mapping method to calculate pricing and additional fields
     */
    @AfterMapping
    default void calculatePricing(@MappingTarget RoomAvailabilityDto dto, Room room) {
        if (dto.getCheckInDate() != null && dto.getCheckOutDate() != null) {
            long nights = ChronoUnit.DAYS.between(dto.getCheckInDate(), dto.getCheckOutDate());
            dto.setNights(nights);
            
            if (room.getBasePrice() != null && nights > 0) {
                BigDecimal totalPrice = room.getBasePrice().multiply(BigDecimal.valueOf(nights));
                dto.setTotalPrice(totalPrice);
            }
        }
    }

    /**
     * After mapping method for check-in room assignment
     */
    @AfterMapping
    default void setCheckInRoomDetails(@MappingTarget com.hotel.reservation.dto.response.CheckInResponseDto.RoomAssignmentDto dto, Room room) {
        // Extract floor from room number (assuming format like "201" = floor 2)
        String roomNumber = room.getRoomNumber();
        if (roomNumber != null && roomNumber.length() >= 1) {
            try {
                Integer floor = Integer.parseInt(roomNumber.substring(0, 1));
                dto.setFloor(floor);
            } catch (NumberFormatException e) {
                dto.setFloor(1); // Default floor
            }
        }
        
        // Generate WiFi credentials
        dto.setWifiNetwork("Hotel_Guest_" + roomNumber);
        dto.setWifiPassword("Welcome2025!");
    }

    /**
     * Custom mapping for room with default rate plan
     */
    @Named("withDefaultRatePlan")
    @AfterMapping
    default void addDefaultRatePlan(@MappingTarget RoomAvailabilityDto dto, Room room) {
        // Add default flexible rate plan
        RoomAvailabilityDto.RatePlanDto ratePlan = RoomAvailabilityDto.RatePlanDto.builder()
                .name("Flexible Rate")
                .includesBreakfast(false)
                .freeCancellation(true)
                .prepaymentRequired(false)
                .benefits(java.util.List.of("Free WiFi", "24/7 Reception"))
                .build();
        dto.setRatePlan(ratePlan);
        
        // Add default cancellation policy
        RoomAvailabilityDto.CancellationPolicyDto cancellationPolicy = 
                RoomAvailabilityDto.CancellationPolicyDto.builder()
                .type("FLEXIBLE")
                .freeCancellationUntil(java.time.LocalDateTime.now().plusDays(1))
                .penaltyPercent(0)
                .description("Free cancellation until 24 hours before check-in")
                .build();
        dto.setCancellationPolicy(cancellationPolicy);
    }

    /**
     * Helper method for room availability with business rules
     */
    @Named("withBusinessRules")
    @AfterMapping
    default void applyBusinessRules(@MappingTarget RoomAvailabilityDto dto, Room room) {
        // Apply early bird discount if applicable
        if (dto.getCheckInDate() != null && 
            dto.getCheckInDate().isAfter(LocalDate.now().plusDays(30))) {
            
            RoomAvailabilityDto.OfferDto earlyBird = RoomAvailabilityDto.OfferDto.builder()
                    .name("Early Bird Discount")
                    .discountPercent(15)
                    .discountAmount(dto.getTotalPrice() != null ? 
                        dto.getTotalPrice().multiply(BigDecimal.valueOf(0.15)) : BigDecimal.ZERO)
                    .description("Book 30 days in advance and save 15%")
                    .validUntil(LocalDate.now().plusDays(7))
                    .build();
            
            dto.setOffers(java.util.List.of(earlyBird));
            
            // Update total price with discount
            if (dto.getTotalPrice() != null) {
                BigDecimal discountedPrice = dto.getTotalPrice().subtract(earlyBird.getDiscountAmount());
                dto.setTotalPrice(discountedPrice);
            }
        }
        
        // Set instant booking based on room status
        dto.setInstantBooking(room.getStatus() == Room.RoomStatus.AVAILABLE);
    }
}