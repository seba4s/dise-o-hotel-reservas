package com.hotel.reservation.mapper;

import com.hotel.reservation.dto.request.CreateReservationDto;
import com.hotel.reservation.dto.response.ReservationResponseDto;
import com.hotel.reservation.model.Reservation;
import com.hotel.reservation.model.Room;
import com.hotel.reservation.model.User;
import org.mapstruct.*;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for Reservation entity and DTOs
 * Handles conversions for HU004 (Create Reservation) and related operations
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {DateTimeMapper.class, UserMapper.class, RoomMapper.class}
)
public interface ReservationMapper {

    /**
     * Convert Reservation entity to ReservationResponseDto
     */
    @Mapping(target = "id", source = "reservation.id")
    @Mapping(target = "confirmationNumber", source = "reservation.confirmationNumber")
    @Mapping(target = "guest", source = "reservation.guest", qualifiedByName = "toGuestDto")
    @Mapping(target = "room", source = "reservation.room", qualifiedByName = "toRoomDto")
    @Mapping(target = "checkInDate", source = "reservation.checkInDate")
    @Mapping(target = "checkOutDate", source = "reservation.checkOutDate")
    @Mapping(target = "adults", source = "reservation.adults")
    @Mapping(target = "children", source = "reservation.children")
    @Mapping(target = "totalAmount", source = "reservation.totalAmount")
    @Mapping(target = "currency", constant = "COP")
    @Mapping(target = "status", source = "reservation.status")
    @Mapping(target = "specialRequests", source = "reservation.specialRequests")
    @Mapping(target = "nights", ignore = true) // Calculated in @AfterMapping
    @Mapping(target = "pricing", ignore = true) // Set separately if needed
    @Mapping(target = "actualCheckInTime", source = "reservation.actualCheckInTime")
    @Mapping(target = "actualCheckOutTime", source = "reservation.actualCheckOutTime")
    @Mapping(target = "checkInStaff", source = "reservation.checkInStaff")
    @Mapping(target = "checkOutStaff", source = "reservation.checkOutStaff")
    @Mapping(target = "additionalCharges", ignore = true) // Mapped separately
    @Mapping(target = "paymentMethod", source = "reservation.paymentMethod")
    @Mapping(target = "paymentStatus", source = "reservation.paymentStatus")
    @Mapping(target = "paymentTransactionId", source = "reservation.paymentTransactionId")
    @Mapping(target = "promoCode", ignore = true) // Set separately if available
    @Mapping(target = "ratePlan", ignore = true) // Set separately if available
    @Mapping(target = "includeBreakfast", ignore = true) // Set separately if available
    @Mapping(target = "airportTransfer", ignore = true) // Set separately if available
    @Mapping(target = "createdAt", source = "reservation.createdAt")
    @Mapping(target = "updatedAt", source = "reservation.updatedAt")
    @Mapping(target = "cancelledAt", ignore = true) // Set if cancelled
    @Mapping(target = "cancellationReason", ignore = true) // Set if cancelled
    ReservationResponseDto toResponseDto(Reservation reservation);

    /**
     * Convert CreateReservationDto to Reservation entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "confirmationNumber", ignore = true) // Generated separately
    @Mapping(target = "guest", ignore = true) // Set separately
    @Mapping(target = "room", ignore = true) // Set separately
    @Mapping(target = "checkInDate", source = "checkInDate")
    @Mapping(target = "checkOutDate", source = "checkOutDate")
    @Mapping(target = "adults", source = "adults")
    @Mapping(target = "children", source = "children")
    @Mapping(target = "totalAmount", ignore = true) // Calculated separately
    @Mapping(target = "status", constant = "PRE_RESERVATION")
    @Mapping(target = "specialRequests", source = "specialRequests")
    @Mapping(target = "actualCheckInTime", ignore = true)
    @Mapping(target = "actualCheckOutTime", ignore = true)
    @Mapping(target = "checkInStaff", ignore = true)
    @Mapping(target = "checkOutStaff", ignore = true)
    @Mapping(target = "additionalCharges", ignore = true)
    @Mapping(target = "paymentMethod", source = "paymentMethod")
    @Mapping(target = "paymentStatus", ignore = true) // Set separately
    @Mapping(target = "paymentTransactionId", ignore = true) // Set separately
    @Mapping(target = "createdAt", ignore = true) // Set by @CreatedDate
    @Mapping(target = "updatedAt", ignore = true) // Set by @LastModifiedDate
    Reservation toEntity(CreateReservationDto dto);

    /**
     * Update existing Reservation entity with CreateReservationDto
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "confirmationNumber", ignore = true) // Don't change
    @Mapping(target = "guest", ignore = true) // Handle separately
    @Mapping(target = "room", ignore = true) // Handle separately
    @Mapping(target = "status", ignore = true) // Don't change through update
    @Mapping(target = "actualCheckInTime", ignore = true)
    @Mapping(target = "actualCheckOutTime", ignore = true)
    @Mapping(target = "checkInStaff", ignore = true)
    @Mapping(target = "checkOutStaff", ignore = true)
    @Mapping(target = "additionalCharges", ignore = true)
    @Mapping(target = "paymentStatus", ignore = true)
    @Mapping(target = "paymentTransactionId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(CreateReservationDto dto, @MappingTarget Reservation entity);

    /**
     * Map User to GuestDto
     */
    @Named("toGuestDto")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "country", source = "country")
    ReservationResponseDto.GuestDto toGuestDto(User user);

    /**
     * Map Room to RoomDto
     */
    @Named("toRoomDto")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "roomNumber", source = "roomNumber")
    @Mapping(target = "roomType", source = "roomType")
    @Mapping(target = "capacity", source = "capacity")
    @Mapping(target = "bedType", source = "bedType")
    @Mapping(target = "basePrice", source = "basePrice")
    @Mapping(target = "amenities", source = "amenities")
    ReservationResponseDto.RoomDto toRoomDto(Room room);

    /**
     * Map additional charges from Reservation to DTOs
     */
    @Named("toAdditionalChargeDto")
    default List<ReservationResponseDto.AdditionalChargeDto> toAdditionalChargeDtos(
            List<Reservation.AdditionalCharge> additionalCharges) {
        
        if (additionalCharges == null || additionalCharges.isEmpty()) {
            return List.of();
        }
        
        return additionalCharges.stream()
                .map(charge -> ReservationResponseDto.AdditionalChargeDto.builder()
                        .description(charge.getDescription())
                        .amount(charge.getAmount())
                        .type(charge.getType())
                        .addedAt(charge.getChargedAt())
                        .addedBy(charge.getChargedBy())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * After mapping method to calculate nights and additional fields
     */
    @AfterMapping
    default void calculateNights(@MappingTarget ReservationResponseDto dto, Reservation reservation) {
        if (reservation.getCheckInDate() != null && reservation.getCheckOutDate() != null) {
            long nights = ChronoUnit.DAYS.between(
                reservation.getCheckInDate(), 
                reservation.getCheckOutDate()
            );
            dto.setNights(nights);
        }
        
        // Map additional charges if they exist
        if (reservation.getAdditionalCharges() != null && !reservation.getAdditionalCharges().isEmpty()) {
            List<ReservationResponseDto.AdditionalChargeDto> chargeDtos = 
                toAdditionalChargeDtos(reservation.getAdditionalCharges());
            dto.setAdditionalCharges(chargeDtos);
        }
    }

    /**
     * Before mapping validation
     */
    @BeforeMapping
    default void validateReservation(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation cannot be null");
        }
        if (reservation.getGuest() == null) {
            throw new IllegalArgumentException("Reservation must have a guest");
        }
        if (reservation.getRoom() == null) {
            throw new IllegalArgumentException("Reservation must have a room");
        }
    }
}