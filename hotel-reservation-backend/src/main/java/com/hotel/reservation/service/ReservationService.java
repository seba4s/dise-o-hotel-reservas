package com.hotel.reservation.service;

import com.hotel.reservation.dto.request.CreateReservationDto;
import com.hotel.reservation.dto.response.ReservationResponseDto;
import com.hotel.reservation.exception.*;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final ReservationMapper reservationMapper;
    private final AvailabilityService availabilityService;

    /**
     * HU004: Create new reservation (pre-reservation state)
     */
    @Transactional
    public ReservationResponseDto createReservation(CreateReservationDto reservationRequest, String currentUser) {
        log.info("Creating reservation for user: {} - Room: {}, Dates: {} to {}", 
                currentUser, reservationRequest.getRoomId(), 
                reservationRequest.getCheckInDate(), reservationRequest.getCheckOutDate());

        // Validate request
        validateReservationRequest(reservationRequest);

        // Get or create guest
        User guest = resolveGuest(reservationRequest, currentUser);

        // Get room and validate availability
        Room room = getRoomAndValidateAvailability(reservationRequest);

        // Calculate pricing
        BigDecimal totalAmount = calculateReservationTotal(room, reservationRequest);

        // Create reservation entity
        Reservation reservation = buildReservation(reservationRequest, guest, room, totalAmount);

        // Generate confirmation number
        reservation.setConfirmationNumber(generateConfirmationNumber());

        // Save reservation
        reservation = reservationRepository.save(reservation);

        log.info("Reservation created successfully - ID: {}, Confirmation: {}", 
                reservation.getId(), reservation.getConfirmationNumber());

        // TODO: Send confirmation email
        // emailService.sendReservationConfirmation(reservation);

        return reservationMapper.toResponseDto(reservation);
    }

    /**
     * Get reservation by confirmation number
     */
    @Transactional(readOnly = true)
    public ReservationResponseDto getReservationByConfirmation(String confirmationNumber, String currentUser) {
        log.info("Getting reservation by confirmation: {} for user: {}", confirmationNumber, currentUser);

        Reservation reservation = reservationRepository.findByConfirmationNumber(confirmationNumber)
                .orElseThrow(() -> ResourceNotFoundException.reservationByConfirmation(confirmationNumber));

        // Check if user has access to this reservation
        validateReservationAccess(reservation, currentUser);

        return reservationMapper.toResponseDto(reservation);
    }

    /**
     * Get reservation by ID
     */
    @Transactional(readOnly = true)
    public ReservationResponseDto getReservationById(String reservationId, String currentUser) {
        log.info("Getting reservation by ID: {} for user: {}", reservationId, currentUser);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> ResourceNotFoundException.reservation(reservationId));

        // Check if user has access to this reservation
        validateReservationAccess(reservation, currentUser);

        return reservationMapper.toResponseDto(reservation);
    }

    /**
     * Get user's reservations
     */
    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getUserReservations(String currentUser) {
        log.info("Getting reservations for user: {}", currentUser);

        User user = userRepository.findByUsername(currentUser)
                .orElseThrow(() -> ResourceNotFoundException.userByUsername(currentUser));

        List<Reservation> reservations = reservationRepository.findByGuestIdOrderByCreatedAtDesc(user.getId());

        return reservations.stream()
                .map(reservationMapper::toResponseDto)
                .toList();
    }

    /**
     * Update existing reservation (only in pre-reservation state)
     */
    @Transactional
    public ReservationResponseDto updateReservation(String reservationId, 
                                                   CreateReservationDto updateRequest, 
                                                   String currentUser) {
        log.info("Updating reservation: {} for user: {}", reservationId, currentUser);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> ResourceNotFoundException.reservation(reservationId));

        // Validate access and state
        validateReservationAccess(reservation, currentUser);
        validateReservationCanBeModified(reservation);

        // If room or dates changed, validate new availability
        if (!reservation.getRoom().getId().equals(updateRequest.getRoomId()) ||
            !reservation.getCheckInDate().equals(updateRequest.getCheckInDate()) ||
            !reservation.getCheckOutDate().equals(updateRequest.getCheckOutDate())) {
            
            validateRoomAvailabilityForUpdate(updateRequest, reservationId);
        }

        // Update reservation
        reservationMapper.updateEntityFromDto(updateRequest, reservation);

        // Recalculate total if room or dates changed
        if (!reservation.getRoom().getId().equals(updateRequest.getRoomId()) ||
            !reservation.getCheckInDate().equals(updateRequest.getCheckInDate()) ||
            !reservation.getCheckOutDate().equals(updateRequest.getCheckOutDate())) {
            
            Room room = roomRepository.findById(updateRequest.getRoomId())
                    .orElseThrow(() -> ResourceNotFoundException.room(updateRequest.getRoomId()));
            
            BigDecimal newTotal = calculateReservationTotal(room, updateRequest);
            reservation.setTotalAmount(newTotal);
        }

        reservation = reservationRepository.save(reservation);

        log.info("Reservation updated successfully: {}", reservationId);

        return reservationMapper.toResponseDto(reservation);
    }

    /**
     * Cancel existing reservation
     */
    @Transactional
    public ReservationResponseDto cancelReservation(String reservationId, String currentUser) {
        log.info("Cancelling reservation: {} for user: {}", reservationId, currentUser);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> ResourceNotFoundException.reservation(reservationId));

        // Validate access and state
        validateReservationAccess(reservation, currentUser);
        validateReservationCanBeCancelled(reservation);

        // Calculate cancellation fee
        BigDecimal cancellationFee = reservation.calculateCancellationFee();
        
        // Cancel reservation
        reservation.cancel("Guest requested cancellation", currentUser);
        reservation.setCancellationFee(cancellationFee);

        reservation = reservationRepository.save(reservation);

        log.info("Reservation cancelled successfully: {}, Fee: {}", reservationId, cancellationFee);

        // TODO: Process refund if applicable
        // paymentService.processRefund(reservation);

        // TODO: Send cancellation confirmation
        // emailService.sendCancellationConfirmation(reservation);

        return reservationMapper.toResponseDto(reservation);
    }

    /**
     * Validate reservation request
     */
    private void validateReservationRequest(CreateReservationDto request) {
        LocalDate now = LocalDate.now(); // 2025-11-22 based on current time

        // Validate dates
        if (request.getCheckInDate().isBefore(now)) {
            throw BadRequestException.pastDate("checkInDate", request.getCheckInDate().toString());
        }

        if (request.getCheckOutDate().isBefore(request.getCheckInDate()) ||
            request.getCheckOutDate().equals(request.getCheckInDate())) {
            throw BadRequestException.invalidDateRange(
                request.getCheckInDate().toString(),
                request.getCheckOutDate().toString());
        }

        // Validate stay duration
        long nights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        if (nights > 30) {
            throw BadRequestException.exceedsMaxStayDuration(nights, 30L);
        }

        // Validate guest count
        if (request.getTotalGuests() > 10) {
            throw BadRequestException.invalidGuestCount(request.getAdults(), request.getChildren());
        }
    }

    /**
     * Resolve guest user (existing or create new)
     */
    private User resolveGuest(CreateReservationDto request, String currentUser) {
        // If guest info is provided and different from current user, handle accordingly
        User user = userRepository.findByUsername(currentUser)
                .orElseThrow(() -> ResourceNotFoundException.userByUsername(currentUser));

        // For now, use the current authenticated user as the guest
        // In a more complex scenario, we might create a new guest user or link to existing
        
        return user;
    }

    /**
     * Get room and validate availability
     */
    private Room getRoomAndValidateAvailability(CreateReservationDto request) {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> ResourceNotFoundException.room(request.getRoomId()));

        if (!room.getActive()) {
            throw new BadRequestException("Room is not active", "roomId", request.getRoomId());
        }

        if (!room.isAvailable()) {
            throw RoomNotAvailableException.outOfService(room.getId(), room.getRoomNumber());
        }

        // Check room capacity
        if (!room.canAccommodate(request.getTotalGuests())) {
            throw BadRequestException.invalidRoomCapacity(request.getTotalGuests(), room.getCapacity());
        }

        // Check for conflicting reservations
        List<Reservation> conflicts = reservationRepository.findConflictingReservations(
                room.getId(), request.getCheckInDate(), request.getCheckOutDate());

        if (!conflicts.isEmpty()) {
            List<String> conflictingReservationIds = conflicts.stream()
                    .map(Reservation::getConfirmationNumber)
                    .toList();
            
            throw RoomNotAvailableException.forDatesWithConflicts(
                    room.getId(), 
                    room.getRoomNumber(), 
                    request.getCheckInDate(), 
                    request.getCheckOutDate(),
                    conflictingReservationIds);
        }

        return room;
    }

    /**
     * Calculate total reservation amount
     */
    private BigDecimal calculateReservationTotal(Room room, CreateReservationDto request) {
        long nights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        
        BigDecimal baseTotal = room.getBasePrice().multiply(BigDecimal.valueOf(nights));
        
        // Apply seasonal pricing if configured
        BigDecimal seasonalTotal = baseTotal;
        if (room.getSeasonalPricing() != null && !room.getSeasonalPricing().isEmpty()) {
            seasonalTotal = applySeasonalPricing(baseTotal, room, request.getCheckInDate(), request.getCheckOutDate());
        }
        
        // Apply early bird discount if applicable
        BigDecimal finalTotal = seasonalTotal;
        if (request.getCheckInDate().isAfter(LocalDate.now().plusDays(30))) {
            finalTotal = seasonalTotal.multiply(BigDecimal.valueOf(0.85)); // 15% discount
        }
        
        // Add extra guest fees if applicable
        if (request.getTotalGuests() > room.getCapacity() && room.getRules() != null && room.getRules().getAllowExtraBed()) {
            int extraGuests = request.getTotalGuests() - room.getCapacity();
            BigDecimal extraGuestFee = room.getRules().getExtraBedCharge().multiply(BigDecimal.valueOf(extraGuests * nights));
            finalTotal = finalTotal.add(extraGuestFee);
        }
        
        return finalTotal;
    }

    /**
     * Apply seasonal pricing (simplified implementation)
     */
    private BigDecimal applySeasonalPricing(BigDecimal baseTotal, Room room, LocalDate checkIn, LocalDate checkOut) {
        // Simplified - in real implementation would calculate per night
        for (Room.SeasonalPricing seasonal : room.getSeasonalPricing()) {
            if (!checkIn.isBefore(seasonal.getStartDate()) && !checkOut.isAfter(seasonal.getEndDate())) {
                if ("PERCENTAGE".equals(seasonal.getModifierType())) {
                    return baseTotal.multiply(seasonal.getPriceModifier());
                }
            }
        }
        return baseTotal;
    }

    /**
     * Build reservation entity
     */
    private Reservation buildReservation(CreateReservationDto request, User guest, Room room, BigDecimal totalAmount) {
        Reservation reservation = reservationMapper.toEntity(request);
        reservation.setGuest(guest);
        reservation.setRoom(room);
        reservation.setTotalAmount(totalAmount);
        reservation.setStatus(Reservation.ReservationStatus.PRE_RESERVATION);
        
        // Set additional fields
        reservation.setBookingSource("DIRECT");
        reservation.setChannel("WEBSITE");
        
        // Create cancellation policy
        Reservation.CancellationPolicy policy = Reservation.CancellationPolicy.builder()
                .policyType("FLEXIBLE")
                .freeCancellationHours(24)
                .cancellationFeePercent(BigDecimal.valueOf(10))
                .terms("Free cancellation up to 24 hours before check-in")
                .build();
        reservation.setCancellationPolicy(policy);
        
        return reservation;
    }

    /**
     * Generate unique confirmation number
     */
    private String generateConfirmationNumber() {
        String prefix = "CONF";
        String timestamp = String.valueOf(System.currentTimeMillis() % 100000000); // Last 8 digits
        String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        
        String confirmationNumber = prefix + timestamp + random;
        
        // Ensure uniqueness
        while (reservationRepository.existsByConfirmationNumber(confirmationNumber)) {
            random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            confirmationNumber = prefix + timestamp + random;
        }
        
        return confirmationNumber;
    }

    /**
     * Validate user has access to reservation
     */
    private void validateReservationAccess(Reservation reservation, String currentUser) {
        User user = userRepository.findByUsername(currentUser)
                .orElseThrow(() -> ResourceNotFoundException.userByUsername(currentUser));

        // Staff and admin can access any reservation
        if (user.isStaff()) {
            return;
        }

        // Clients can only access their own reservations
        if (!reservation.getGuest().getId().equals(user.getId())) {
            throw ForbiddenException.cannotAccessOtherUserData(reservation.getGuest().getId(), user.getId());
        }
    }

    /**
     * Validate reservation can be modified
     */
    private void validateReservationCanBeModified(Reservation reservation) {
        if (!reservation.canBeModified()) {
            throw InvalidReservationStateException.cannotModify(
                    reservation.getId(),
                    reservation.getConfirmationNumber(),
                    reservation.getStatus());
        }
    }

    /**
     * Validate reservation can be cancelled
     */
    private void validateReservationCanBeCancelled(Reservation reservation) {
        if (!reservation.canBeCancelled()) {
            throw InvalidReservationStateException.cannotCancel(
                    reservation.getId(),
                    reservation.getConfirmationNumber(),
                    reservation.getStatus());
        }
    }

    /**
     * Validate room availability for update (excluding current reservation)
     */
    private void validateRoomAvailabilityForUpdate(CreateReservationDto request, String excludeReservationId) {
        List<Reservation> conflicts = reservationRepository.findConflictingReservationsExcludingId(
                request.getRoomId(), 
                request.getCheckInDate(), 
                request.getCheckOutDate(),
                excludeReservationId);

        if (!conflicts.isEmpty()) {
            Room room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> ResourceNotFoundException.room(request.getRoomId()));
                    
            List<String> conflictingIds = conflicts.stream()
                    .map(Reservation::getConfirmationNumber)
                    .toList();
                    
            throw RoomNotAvailableException.forDatesWithConflicts(
                    room.getId(), 
                    room.getRoomNumber(), 
                    request.getCheckInDate(), 
                    request.getCheckOutDate(),
                    conflictingIds);
        }
    }
}