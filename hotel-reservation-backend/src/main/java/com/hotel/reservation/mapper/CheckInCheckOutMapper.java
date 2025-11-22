package com.hotel.reservation.mapper;

import com.hotel.reservation.dto.request.CheckOutDto;
import com.hotel.reservation.dto.response.CheckInResponseDto;
import com.hotel.reservation.dto.response.CheckOutResponseDto;
import com.hotel.reservation.model.Reservation;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for Check-in and Check-out operations
 * Handles conversions for HU009 (Check-in) and HU010 (Check-out)
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {DateTimeMapper.class, ReservationMapper.class}
)
public interface CheckInCheckOutMapper {

    /**
     * Convert Reservation to CheckOutResponseDto
     */
    @Mapping(target = "checkOutId", ignore = true) // Generated separately
    @Mapping(target = "reservationId", source = "id")
    @Mapping(target = "confirmationNumber", source = "confirmationNumber")
    @Mapping(target = "guest", source = "guest", qualifiedByName = "toCheckOutGuestDto")
    @Mapping(target = "room", source = "room", qualifiedByName = "toCheckOutRoomDto")
    @Mapping(target = "checkOutTime", source = "actualCheckOutTime")
    @Mapping(target = "processedBy", source = "checkOutStaff")
    @Mapping(target = "staySummary", ignore = true) // Set separately
    @Mapping(target = "billing", ignore = true) // Set separately
    @Mapping(target = "additionalCharges", ignore = true) // Converted separately
    @Mapping(target = "depositRefund", ignore = true) // Set separately
    @Mapping(target = "roomInspection", ignore = true) // Set separately
    @Mapping(target = "keyCardReturn", ignore = true) // Set separately
    @Mapping(target = "invoice", ignore = true) // Set separately
    @Mapping(target = "guestFeedback", ignore = true) // Set separately
    @Mapping(target = "lostItems", ignore = true) // Set separately
    @Mapping(target = "finalAmount", source = "totalAmount")
    @Mapping(target = "currency", constant = "COP")
    @Mapping(target = "status", constant = "COMPLETED")
    @Mapping(target = "notes", ignore = true) // Set separately
    CheckOutResponseDto toCheckOutResponseDto(Reservation reservation);

    /**
     * Convert Reservation to CheckInResponseDto
     */
    @Mapping(target = "checkInId", ignore = true)
    @Mapping(target = "reservationId", source = "id")
    @Mapping(target = "confirmationNumber", source = "confirmationNumber")
    @Mapping(target = "guest", source = "guest", qualifiedByName = "toCheckInGuestDto")
    @Mapping(target = "room", source = "room", qualifiedByName = "toCheckInRoomDto")
    @Mapping(target = "checkInTime", source = "actualCheckInTime")
    @Mapping(target = "expectedCheckOutDate", source = "checkOutDate")
    @Mapping(target = "processedBy", source = "checkInStaff")
    @Mapping(target = "documentVerification", ignore = true)
    @Mapping(target = "services", ignore = true)
    @Mapping(target = "deposit", ignore = true)
    @Mapping(target = "keyCards", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "welcomePackageProvided", ignore = true)
    @Mapping(target = "policiesAcknowledged", ignore = true)
    @Mapping(target = "parking", ignore = true)
    @Mapping(target = "emergencyContactVerified", ignore = true)
    @Mapping(target = "status", constant = "COMPLETED")
    CheckInResponseDto toCheckInResponseDto(Reservation reservation);

    /**
     * Map additional charges from CheckOutDto to response DTOs
     */
    default List<CheckOutResponseDto.AdditionalChargeDto> mapAdditionalCharges(
            List<CheckOutDto.AdditionalChargeDto> charges) {
        
        if (charges == null || charges.isEmpty()) {
            return List.of();
        }
        
        return charges.stream()
                .map(charge -> CheckOutResponseDto.AdditionalChargeDto.builder()
                        .chargeDate(java.time.LocalDate.now())
                        .description(charge.getDescription())
                        .amount(charge.getAmount())
                        .category(charge.getType())
                        .quantity(charge.getQuantity())
                        .unitPrice(charge.getUnitPrice())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Map User to CheckOut GuestDto
     */
    @Named("toCheckOutGuestDto")
    @Mapping(target = "fullName", expression = "java(user.getFirstName() + \" \" + user.getLastName())")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "roomNumber", ignore = true) // Set from reservation room
    @Mapping(target = "nightsStayed", ignore = true) // Calculated separately
    @Mapping(target = "satisfactionLevel", ignore = true) // Set separately
    CheckOutResponseDto.GuestDto toCheckOutGuestDto(com.hotel.reservation.model.User user);

    /**
     * Map Room to CheckOut RoomDto
     */
    @Named("toCheckOutRoomDto")
    @Mapping(target = "roomNumber", source = "roomNumber")
    @Mapping(target = "roomType", source = "roomType")
    @Mapping(target = "currentStatus", constant = "NEEDS_CLEANING")
    @Mapping(target = "nextCheckIn", ignore = true) // Set separately
    @Mapping(target = "housekeepingPriority", constant = "STANDARD")
    CheckOutResponseDto.RoomDto toCheckOutRoomDto(com.hotel.reservation.model.Room room);

    /**
     * Map User to CheckIn GuestDto
     */
    @Named("toCheckInGuestDto")
    @Mapping(target = "fullName", expression = "java(user.getFirstName() + \" \" + user.getLastName())")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "adults", ignore = true) // Set from reservation
    @Mapping(target = "children", ignore = true) // Set from reservation
    @Mapping(target = "vipGuest", constant = "false")
    @Mapping(target = "preferences", ignore = true) // Set separately
    CheckInResponseDto.GuestDto toCheckInGuestDto(com.hotel.reservation.model.User user);

    /**
     * Map Room to CheckIn RoomAssignmentDto
     */
    @Named("toCheckInRoomDto")
    @Mapping(target = "roomNumber", source = "roomNumber")
    @Mapping(target = "roomType", source = "roomType")
    @Mapping(target = "floor", ignore = true) // Calculated in room mapper
    @Mapping(target = "features", source = "amenities")
    @Mapping(target = "wifiNetwork", ignore = true) // Set separately
    @Mapping(target = "wifiPassword", ignore = true) // Set separately
    @Mapping(target = "roomRate", source = "basePrice")
    CheckInResponseDto.RoomAssignmentDto toCheckInRoomDto(com.hotel.reservation.model.Room room);

    /**
     * After mapping method for check-in response
     */
    @AfterMapping
    default void setCheckInDetails(@MappingTarget CheckInResponseDto dto, Reservation reservation) {
        // Set guest counts from reservation
        if (dto.getGuest() != null) {
            dto.getGuest().setAdults(reservation.getAdults());
            dto.getGuest().setChildren(reservation.getChildren());
        }
        
        // Generate check-in ID
        dto.setCheckInId("checkin_" + java.time.Instant.now().toEpochMilli());
        
        // Set default key cards
        CheckInResponseDto.KeyCardDto keyCards = 
            CheckInResponseDto.KeyCardDto.builder()
                .cardsIssued(2)
                .cardIds(List.of("KEY001", "KEY002"))
                .expiryDate(reservation.getCheckOutDate())
                .accessLevels(List.of("ROOM", "ELEVATOR", "POOL"))
                .programmedAt(java.time.LocalDateTime.now())
                .build();
        dto.setKeyCards(keyCards);
    }

    /**
     * After mapping method for check-out response
     */
    @AfterMapping
    default void setCheckOutDetails(@MappingTarget CheckOutResponseDto dto, Reservation reservation) {
        // Generate check-out ID
        dto.setCheckOutId("checkout_" + java.time.Instant.now().toEpochMilli());
        
        // Set room number in guest info
        if (dto.getGuest() != null && reservation.getRoom() != null) {
            dto.getGuest().setRoomNumber(reservation.getRoom().getRoomNumber());
            
            // Calculate nights stayed
            if (reservation.getActualCheckInTime() != null && reservation.getActualCheckOutTime() != null) {
                long nights = ChronoUnit.DAYS.between(
                    reservation.getActualCheckInTime().toLocalDate(),
                    reservation.getActualCheckOutTime().toLocalDate()
                );
                dto.getGuest().setNightsStayed((int) Math.max(1, nights)); // Minimum 1 night
            } else if (reservation.getCheckInDate() != null && reservation.getCheckOutDate() != null) {
                long nights = ChronoUnit.DAYS.between(
                    reservation.getCheckInDate(),
                    reservation.getCheckOutDate()
                );
                dto.getGuest().setNightsStayed((int) nights);
            }
        }
        
        // Create stay summary
        CheckOutResponseDto.StaySummaryDto staySummary = CheckOutResponseDto.StaySummaryDto.builder()
                .actualCheckIn(reservation.getActualCheckInTime())
                .actualCheckOut(reservation.getActualCheckOutTime())
                .stayDuration(calculateStayDuration(reservation))
                .nightsStayed(dto.getGuest() != null ? dto.getGuest().getNightsStayed() : 0)
                .servicesUsed(List.of("Room Service", "WiFi")) // Default services
                .lateCheckout(false)
                .earlyCheckin(false)
                .build();
        dto.setStaySummary(staySummary);
        
        // Create default billing
        CheckOutResponseDto.BillingDto billing = CheckOutResponseDto.BillingDto.builder()
                .roomCharges(reservation.getTotalAmount())
                .servicesCharges(java.math.BigDecimal.ZERO)
                .taxes(reservation.getTotalAmount().multiply(java.math.BigDecimal.valueOf(0.19))) // 19% tax
                .serviceFees(java.math.BigDecimal.valueOf(50000)) // Default service fee
                .totalBeforeDeposit(reservation.getTotalAmount().multiply(java.math.BigDecimal.valueOf(1.19)).add(java.math.BigDecimal.valueOf(50000)))
                .depositUsed(java.math.BigDecimal.valueOf(200000)) // Default deposit
                .additionalPayment(java.math.BigDecimal.ZERO)
                .paymentMethod("CREDIT_CARD")
                .transactionId("txn_" + java.time.Instant.now().toEpochMilli())
                .build();
        dto.setBilling(billing);
        
        // Create default room inspection
        CheckOutResponseDto.RoomInspectionDto roomInspection = CheckOutResponseDto.RoomInspectionDto.builder()
                .overallCondition("GOOD")
                .cleanliness("EXCELLENT")
                .damageAssessment("NO_DAMAGE")
                .maintenanceIssues(List.of())
                .missingItems(List.of())
                .housekeepingNotes("Room left in excellent condition")
                .inspectedBy("housekeeping_staff")
                .inspectionTime(LocalDateTime.now())
                .build();
        dto.setRoomInspection(roomInspection);
        
        // Create default key card return
        CheckOutResponseDto.KeyCardReturnDto keyCardReturn = CheckOutResponseDto.KeyCardReturnDto.builder()
                .cardsIssued(2)
                .cardsReturned(2)
                .missingCards(0)
                .returnedCardIds(List.of("KEY001", "KEY002"))
                .missingCardFee(java.math.BigDecimal.ZERO)
                .cardsDeactivated(true)
                .build();
        dto.setKeyCardReturn(keyCardReturn);
        
        // Create default invoice
        CheckOutResponseDto.InvoiceDto invoice = CheckOutResponseDto.InvoiceDto.builder()
                .invoiceNumber("INV-2025-" + String.format("%06d", java.time.Instant.now().toEpochMilli() % 1000000))
                .invoiceDate(LocalDateTime.now())
                .invoiceUrl("https://hotel.com/invoices/" + dto.getCheckOutId() + ".pdf")
                .emailSent(true)
                .printProvided(false)
                .build();
        dto.setInvoice(invoice);
    }

    /**
     * Helper method to calculate stay duration
     */
    default String calculateStayDuration(Reservation reservation) {
        if (reservation.getActualCheckInTime() == null || reservation.getActualCheckOutTime() == null) {
            return "Unknown duration";
        }
        
        LocalDateTime checkIn = reservation.getActualCheckInTime();
        LocalDateTime checkOut = reservation.getActualCheckOutTime();
        
        long days = ChronoUnit.DAYS.between(checkIn, checkOut);
        long hours = ChronoUnit.HOURS.between(checkIn.plusDays(days), checkOut);
        long minutes = ChronoUnit.MINUTES.between(checkIn.plusDays(days).plusHours(hours), checkOut);
        
        if (days > 0) {
            return String.format("%d days, %d hours, %d minutes", days, hours, minutes);
        } else if (hours > 0) {
            return String.format("%d hours, %d minutes", hours, minutes);
        } else {
            return String.format("%d minutes", minutes);
        }
    }

    /**
     * Create default deposit info for check-in
     */
    default CheckInResponseDto.DepositDto createDefaultDeposit() {
        return CheckInResponseDto.DepositDto.builder()
                .amount(java.math.BigDecimal.valueOf(200000))
                .currency("COP")
                .paymentMethod("CREDIT_CARD")
                .authorizationCode("AUTH" + java.time.Instant.now().toEpochMilli())
                .collectedAt(LocalDateTime.now())
                .refundable(true)
                .build();
    }
}