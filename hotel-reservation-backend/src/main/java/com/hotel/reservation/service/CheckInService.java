package com.hotel.reservation.service;

import com.hotel.reservation.dto.request.CheckInDto;
import com.hotel.reservation.dto.response.CheckInResponseDto;
import com.hotel.reservation.dto.response.ReservationResponseDto;
import com.hotel.reservation.exception.*;
import com.hotel.reservation.mapper.CheckInCheckOutMapper;
import com.hotel.reservation.mapper.ReservationMapper;
import com.hotel.reservation.model.Reservation;
import com.hotel.reservation.model.Room;
import com.hotel.reservation.model.User;
import com.hotel.reservation.repository.ReservationRepository;
import com.hotel.reservation.repository.RoomRepository;
import com.hotel.reservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckInService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final ReservationMapper reservationMapper;
    private final CheckInCheckOutMapper checkInCheckOutMapper;

    /**
     * HU009: Process guest check-in with identity verification
     */
    @Transactional
    public CheckInResponseDto processCheckIn(CheckInDto checkInRequest, String staffMember) {
        log.info("Processing check-in by staff: {} for reservation: {}", 
                staffMember, checkInRequest.getReservationId());

        // Validate staff permissions
        validateStaffPermissions(staffMember);

        // Get and validate reservation
        Reservation reservation = getAndValidateReservation(checkInRequest.getReservationId());

        // Validate reservation state for check-in
        validateReservationForCheckIn(reservation);

        // Validate guest document
        validateGuestDocument(checkInRequest, reservation.getGuest());

        // Prepare room for guest
        prepareRoomForGuest(reservation.getRoom());

        // Create check-in details
        Reservation.CheckInDetails checkInDetails = buildCheckInDetails(checkInRequest);

        // Process check-in
        reservation.processCheckIn(staffMember, checkInDetails);

        // Update room status
        reservation.getRoom().markAsOccupied();
        roomRepository.save(reservation.getRoom());

        // Save reservation
        reservation = reservationRepository.save(reservation);

        log.info("Check-in processed successfully - Reservation: {}, Room: {}, Staff: {}", 
                reservation.getConfirmationNumber(), 
                reservation.getRoom().getRoomNumber(), 
                staffMember);

        // Build response
        CheckInResponseDto response = checkInCheckOutMapper.toCheckInResponseDto(reservation);
        
        // Set check-in specific details
        enrichCheckInResponse(response, checkInRequest, staffMember);

        // TODO: Send welcome email/SMS
        // notificationService.sendWelcomeMessage(reservation);

        return response;
    }

    /**
     * Get today's check-ins
     */
    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getTodaysCheckIns() {
        LocalDate today = LocalDate.of(2025, 11, 22); // Current date
        log.info("Getting check-ins for today: {}", today);

        List<Reservation> reservations = reservationRepository.findReservationsForCheckInToday(today);
        
        return reservations.stream()
                .map(reservationMapper::toResponseDto)
                .toList();
    }

    /**
     * Get check-ins by specific date
     */
    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getCheckInsByDate(LocalDate date) {
        log.info("Getting check-ins for date: {}", date);

        List<Reservation> reservations = reservationRepository.findReservationsForCheckInToday(date);
        
        return reservations.stream()
                .map(reservationMapper::toResponseDto)
                .toList();
    }

    /**
     * Validate reservation for check-in
     */
    @Transactional(readOnly = true)
    public ReservationResponseDto validateReservationForCheckIn(String confirmationNumber) {
        log.info("Validating reservation for check-in: {}", confirmationNumber);

        Reservation reservation = reservationRepository.findByConfirmationNumber(confirmationNumber)
                .orElseThrow(() -> ResourceNotFoundException.reservationByConfirmation(confirmationNumber));

        // Validate reservation state
        if (reservation.getStatus() != Reservation.ReservationStatus.CONFIRMED) {
            throw InvalidReservationStateException.cannotCheckIn(
                    reservation.getId(), 
                    reservation.getConfirmationNumber(), 
                    reservation.getStatus());
        }

        // Check if check-in date is today
        LocalDate today = LocalDate.of(2025, 11, 22);
        if (!reservation.getCheckInDate().equals(today)) {
            if (reservation.getCheckInDate().isAfter(today)) {
                throw new BadRequestException(
                        String.format("Check-in date is %s, cannot check in before that date", 
                                     reservation.getCheckInDate()),
                        "checkInDate", reservation.getCheckInDate().toString());
            } else {
                // Late check-in - allow but log
                log.warn("Late check-in for reservation {}, scheduled date was {}", 
                        confirmationNumber, reservation.getCheckInDate());
            }
        }

        return reservationMapper.toResponseDto(reservation);
    }

    /**
     * Search reservations for check-in by guest info
     */
    @Transactional(readOnly = true)
    public List<ReservationResponseDto> searchReservationsForCheckIn(String email, String lastName, String documentNumber) {
        log.info("Searching reservations for check-in - Email: {}, LastName: {}, Document: {}", 
                email, lastName, documentNumber);

        String searchTerm = buildSearchTerm(email, lastName, documentNumber);
        if (searchTerm.trim().isEmpty()) {
            throw new BadRequestException("At least one search parameter is required");
        }

        LocalDate today = LocalDate.of(2025, 11, 22);
        LocalDate tomorrow = today.plusDays(1);
        
        List<Reservation> reservations = reservationRepository.searchReservationsForCheckIn(
                searchTerm, today.minusDays(1), tomorrow); // Include yesterday for late check-ins

        return reservations.stream()
                .map(reservationMapper::toResponseDto)
                .toList();
    }

    /**
     * Get check-in history
     */
    @Transactional(readOnly = true)
    public List<CheckInResponseDto> getCheckInHistory(Integer days) {
        log.info("Getting check-in history for last {} days", days);

        LocalDateTime since = LocalDateTime.of(2025, 11, 22, 4, 14, 31).minusDays(days);
        LocalDateTime now = LocalDateTime.of(2025, 11, 22, 4, 14, 31);

        List<Reservation> checkedInReservations = reservationRepository.findByCreatedAtBetween(since, now)
                .stream()
                .filter(r -> r.getStatus() == Reservation.ReservationStatus.CHECKED_IN || 
                           r.getStatus() == Reservation.ReservationStatus.CHECKED_OUT)
                .filter(r -> r.getActualCheckInTime() != null)
                .toList();

        return checkedInReservations.stream()
                .map(reservation -> {
                    CheckInResponseDto response = checkInCheckOutMapper.toCheckInResponseDto(reservation);
                    // Set historical data
                    response.setProcessedBy(reservation.getCheckInStaff());
                    response.setCheckInTime(reservation.getActualCheckInTime());
                    return response;
                })
                .toList();
    }

    /**
     * Validate staff has check-in permissions
     */
    private void validateStaffPermissions(String staffMember) {
        User staff = userRepository.findByUsername(staffMember)
                .orElseThrow(() -> ResourceNotFoundException.userByUsername(staffMember));

        if (!staff.isStaff()) {
            throw ForbiddenException.staffOnlyOperation("check-in");
        }

        if (!staff.isAccountNonLocked() || !staff.isEnabled()) {
            throw new ForbiddenException("Staff account is not active", "STAFF", staff.getRole().name());
        }
    }

    /**
     * Get and validate reservation for check-in
     */
    private Reservation getAndValidateReservation(String reservationId) {
        // Try by ID first, then by confirmation number
        Reservation reservation = reservationRepository.findById(reservationId)
                .or(() -> reservationRepository.findByConfirmationNumber(reservationId))
                .orElseThrow(() -> ResourceNotFoundException.reservation(reservationId));

        return reservation;
    }

    /**
     * Validate reservation state for check-in
     */
    private void validateReservationForCheckIn(Reservation reservation) {
        if (!reservation.canCheckIn()) {
            throw InvalidReservationStateException.cannotCheckIn(
                    reservation.getId(),
                    reservation.getConfirmationNumber(),
                    reservation.getStatus());
        }

        if (reservation.getActualCheckInTime() != null) {
            throw InvalidReservationStateException.alreadyCheckedIn(
                    reservation.getId(),
                    reservation.getConfirmationNumber());
        }
    }

    /**
     * Validate guest document information
     */
    private void validateGuestDocument(CheckInDto checkInRequest, User guest) {
        if (!checkInRequest.hasCompleteDocumentInfo()) {
            throw BadRequestException.missingRequiredField("document information");
        }

        // Validate document type
        List<String> validDocumentTypes = List.of("PASSPORT", "NATIONAL_ID", "DRIVER_LICENSE", "OTHER");
        if (!validDocumentTypes.contains(checkInRequest.getDocumentType())) {
            throw BadRequestException.invalidDocumentType(checkInRequest.getDocumentType());
        }

        // TODO: Additional document validation could be added here
        // documentValidationService.validateDocument(checkInRequest.getDocumentType(), checkInRequest.getDocumentNumber());

        log.info("Document validated for guest: {} - Type: {}, Number: {}", 
                guest.getEmail(), checkInRequest.getDocumentType(), checkInRequest.getDocumentNumber());
    }

    /**
     * Prepare room for guest arrival
     */
    private void prepareRoomForGuest(Room room) {
        if (room.getStatus() != Room.RoomStatus.AVAILABLE) {
            if (room.getStatus() == Room.RoomStatus.CLEANING) {
                log.warn("Room {} is still being cleaned, check-in may be delayed", room.getRoomNumber());
            } else {
                throw new ConflictException(
                        String.format("Room %s is not ready for check-in, current status: %s", 
                                     room.getRoomNumber(), room.getStatus()),
                        "ROOM_NOT_READY");
            }
        }

        // TODO: Notify housekeeping that room is being occupied
        // housekeepingService.notifyRoomOccupied(room.getId());
    }

    /**
     * Build check-in details from request
     */
    private Reservation.CheckInDetails buildCheckInDetails(CheckInDto checkInRequest) {
        return Reservation.CheckInDetails.builder()
                .documentType(checkInRequest.getDocumentType())
                .documentNumber(checkInRequest.getDocumentNumber())
                .documentCountry(checkInRequest.getDocumentCountry())
                .documentVerified(true)
                .emergencyContactName(checkInRequest.getEmergencyContactName())
                .emergencyContactPhone(checkInRequest.getEmergencyContactPhone())
                .vehicleInfo(checkInRequest.getVehicleInfo())
                .keyCardsIssued(List.of("KEY_" + UUID.randomUUID().toString().substring(0, 8)))
                .keyCardsExpiry(LocalDateTime.of(2025, 11, 25, 12, 0)) // Check-out date + time
                .depositAmount(BigDecimal.valueOf(200000)) // Standard deposit
                .depositPaymentMethod("CREDIT_CARD")
                .depositAuthCode("AUTH_" + System.currentTimeMillis())
                .termsAccepted(checkInRequest.getTermsAccepted())
                .damageWaiverSigned(true)
                .staffNotes(checkInRequest.getNotes())
                .servicesRequested(parseAdditionalServices(checkInRequest.getAdditionalServices()))
                .build();
    }

    /**
     * Enrich check-in response with additional details
     */
    private void enrichCheckInResponse(CheckInResponseDto response, CheckInDto request, String staffMember) {
        // Set document verification details
        CheckInResponseDto.DocumentVerificationDto docVerification = 
                CheckInResponseDto.DocumentVerificationDto.builder()
                .documentType(request.getDocumentType())
                .documentNumber(request.getDocumentNumber())
                .issuingCountry(request.getDocumentCountry())
                .verificationStatus("VERIFIED")
                .verifiedAt(LocalDateTime.of(2025, 11, 22, 4, 14, 31))
                .documentCopyStored(true)
                .build();
        response.setDocumentVerification(docVerification);

        // Set default services
        List<CheckInResponseDto.ServiceDto> services = List.of(
                CheckInResponseDto.ServiceDto.builder()
                        .name("WiFi")
                        .description("Free wireless internet access")
                        .cost(BigDecimal.ZERO)
                        .included(true)
                        .activePeriod("During entire stay")
                        .build(),
                CheckInResponseDto.ServiceDto.builder()
                        .name("Daily Housekeeping")
                        .description("Daily room cleaning service")
                        .cost(BigDecimal.ZERO)
                        .included(true)
                        .activePeriod("Daily at 10:00 AM")
                        .build()
        );
        response.setServices(services);

        // Set deposit information
        CheckInResponseDto.DepositDto deposit = checkInCheckOutMapper.createDefaultDeposit();
        response.setDeposit(deposit);

        // Set parking if vehicle info provided
        if (request.getVehicleInfo() != null && !request.getVehicleInfo().trim().isEmpty()) {
            CheckInResponseDto.ParkingDto parking = CheckInResponseDto.ParkingDto.builder()
                    .spaceNumber("P-A" + (int)(Math.random() * 100 + 1))
                    .licensePlate(extractLicensePlate(request.getVehicleInfo()))
                    .vehicleInfo(request.getVehicleInfo())
                    .parkingFee(BigDecimal.valueOf(15000)) // Daily parking fee
                    .includedInRate(false)
                    .build();
            response.setParking(parking);
        }

        // Set emergency contact verification
        response.setEmergencyContactVerified(request.hasEmergencyContact());
        
        // Set welcome package
        response.setWelcomePackageProvided(true);
        
        // Set policies acknowledgment
        response.setPoliciesAcknowledged(request.getTermsAccepted());
        
        // Set notes
        response.setNotes(request.getNotes());
    }

    /**
     * Build search term from available parameters
     */
    private String buildSearchTerm(String email, String lastName, String documentNumber) {
        if (email != null && !email.trim().isEmpty()) {
            return email.trim();
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            return lastName.trim();
        }
        if (documentNumber != null && !documentNumber.trim().isEmpty()) {
            return documentNumber.trim();
        }
        return "";
    }

    /**
     * Parse additional services string
     */
    private List<String> parseAdditionalServices(String servicesString) {
        if (servicesString == null || servicesString.trim().isEmpty()) {
            return List.of();
        }
        
        return List.of(servicesString.split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    /**
     * Extract license plate from vehicle info
     */
    private String extractLicensePlate(String vehicleInfo) {
        // Simple extraction - in real implementation would use regex
        String[] parts = vehicleInfo.split(" ");
        return parts.length > 0 ? parts[0] : "UNKNOWN";
    }
}