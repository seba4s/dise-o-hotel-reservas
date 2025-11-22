package com.hotel.reservation.service;

import com.hotel.reservation.dto.request.CheckOutDto;
import com.hotel.reservation.dto.response.CheckOutResponseDto;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckOutService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final ReservationMapper reservationMapper;
    private final CheckInCheckOutMapper checkInCheckOutMapper;

    /**
     * HU010: Process guest check-out with additional charges and room status update
     */
    @Transactional
    public CheckOutResponseDto processCheckOut(CheckOutDto checkOutRequest, String staffMember) {
        log.info("Processing check-out by staff: {} for reservation: {}", 
                staffMember, checkOutRequest.getReservationId());

        // Validate staff permissions
        validateStaffPermissions(staffMember);

        // Get and validate reservation
        Reservation reservation = getAndValidateReservation(checkOutRequest.getReservationId());

        // Validate reservation state for check-out
        validateReservationForCheckOut(reservation);

        // Process additional charges
        BigDecimal additionalChargesTotal = processAdditionalCharges(reservation, checkOutRequest);

        // Create check-out details
        Reservation.CheckOutDetails checkOutDetails = buildCheckOutDetails(checkOutRequest, additionalChargesTotal);

        // Process check-out
        reservation.processCheckOut(staffMember, checkOutDetails);

        // Update room status for housekeeping
        updateRoomStatusAfterCheckOut(reservation.getRoom(), checkOutRequest);

        // Calculate final amount
        BigDecimal finalAmount = calculateFinalAmount(reservation, additionalChargesTotal);
        reservation.setTotalAmount(finalAmount);

        // Save reservation
        reservation = reservationRepository.save(reservation);

        log.info("Check-out processed successfully - Reservation: {}, Room: {}, Final amount: {}", 
                reservation.getConfirmationNumber(), 
                reservation.getRoom().getRoomNumber(), 
                finalAmount);

        // Build response
        CheckOutResponseDto response = checkInCheckOutMapper.toCheckOutResponseDto(reservation);
        
        // Set check-out specific details
        enrichCheckOutResponse(response, checkOutRequest, staffMember, additionalChargesTotal);

        // TODO: Process final payment if needed
        // paymentService.processFinalPayment(reservation, additionalChargesTotal);

        // TODO: Send receipt and feedback request
        // notificationService.sendCheckOutReceipt(reservation);
        // notificationService.sendFeedbackRequest(reservation);

        return response;
    }

    /**
     * Get today's check-outs
     */
    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getTodaysCheckOuts() {
        LocalDate today = LocalDate.of(2025, 11, 22); // Current date
        log.info("Getting check-outs for today: {}", today);

        List<Reservation> reservations = reservationRepository.findReservationsForCheckOutToday(today);
        
        return reservations.stream()
                .map(reservationMapper::toResponseDto)
                .toList();
    }

    /**
     * Get check-outs by specific date
     */
    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getCheckOutsByDate(LocalDate date) {
        log.info("Getting check-outs for date: {}", date);

        List<Reservation> reservations = reservationRepository.findReservationsForCheckOutToday(date);
        
        return reservations.stream()
                .map(reservationMapper::toResponseDto)
                .toList();
    }

    /**
     * Get current guest information in a specific room
     */
    @Transactional(readOnly = true)
    public ReservationResponseDto getCurrentGuestInRoom(String roomNumber) {
        log.info("Getting current guest info for room: {}", roomNumber);

        Reservation reservation = reservationRepository.findCurrentGuestInRoom(roomNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("No guest currently checked in to room %s", roomNumber)));

        return reservationMapper.toResponseDto(reservation);
    }

    /**
     * Validate reservation for check-out
     */
    @Transactional(readOnly = true)
    public ReservationResponseDto validateReservationForCheckOut(String confirmationNumber) {
        log.info("Validating reservation for check-out: {}", confirmationNumber);

        Reservation reservation = reservationRepository.findByConfirmationNumber(confirmationNumber)
                .orElseThrow(() -> ResourceNotFoundException.reservationByConfirmation(confirmationNumber));

        // Validate reservation state
        if (reservation.getStatus() != Reservation.ReservationStatus.CHECKED_IN) {
            throw InvalidReservationStateException.cannotCheckOut(
                    reservation.getId(), 
                    reservation.getConfirmationNumber(), 
                    reservation.getStatus());
        }

        return reservationMapper.toResponseDto(reservation);
    }

    /**
     * Get check-out history
     */
    @Transactional(readOnly = true)
    public List<CheckOutResponseDto> getCheckOutHistory(Integer days) {
        log.info("Getting check-out history for last {} days", days);

        LocalDateTime since = LocalDateTime.of(2025, 11, 22, 4, 14, 31).minusDays(days);
        LocalDateTime now = LocalDateTime.of(2025, 11, 22, 4, 14, 31);

        List<Reservation> checkedOutReservations = reservationRepository.findByCreatedAtBetween(since, now)
                .stream()
                .filter(r -> r.getStatus() == Reservation.ReservationStatus.CHECKED_OUT)
                .filter(r -> r.getActualCheckOutTime() != null)
                .toList();

        return checkedOutReservations.stream()
                .map(reservation -> {
                    CheckOutResponseDto response = checkInCheckOutMapper.toCheckOutResponseDto(reservation);
                    // Set historical data
                    response.setProcessedBy(reservation.getCheckOutStaff());
                    response.setCheckOutTime(reservation.getActualCheckOutTime());
                    return response;
                })
                .toList();
    }

    /**
     * Calculate check-out total with additional charges (preview)
     */
    @Transactional(readOnly = true)
    public CheckOutResponseDto calculateCheckOutTotal(String reservationId, 
                                                     List<CheckOutDto.AdditionalChargeDto> additionalCharges) {
        log.info("Calculating check-out total for reservation: {} with {} additional charges", 
                reservationId, additionalCharges != null ? additionalCharges.size() : 0);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> ResourceNotFoundException.reservation(reservationId));

        if (reservation.getStatus() != Reservation.ReservationStatus.CHECKED_IN) {
            throw InvalidReservationStateException.cannotCheckOut(
                    reservation.getId(),
                    reservation.getConfirmationNumber(),
                    reservation.getStatus());
        }

        // Calculate additional charges
        BigDecimal additionalChargesTotal = BigDecimal.ZERO;
        if (additionalCharges != null) {
            additionalChargesTotal = additionalCharges.stream()
                    .map(CheckOutDto.AdditionalChargeDto::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        // Calculate final amount
        BigDecimal finalAmount = calculateFinalAmount(reservation, additionalChargesTotal);

        // Build preview response
        CheckOutResponseDto response = checkInCheckOutMapper.toCheckOutResponseDto(reservation);
        response.setFinalAmount(finalAmount);
        response.setStatus("PREVIEW");
        
        // Set additional charges
        if (additionalCharges != null) {
            List<CheckOutResponseDto.AdditionalChargeDto> chargeDtos = 
                    checkInCheckOutMapper.mapAdditionalCharges(additionalCharges);
            response.setAdditionalCharges(chargeDtos);
        }

        return response;
    }

    /**
     * Validate staff has check-out permissions
     */
    private void validateStaffPermissions(String staffMember) {
        User staff = userRepository.findByUsername(staffMember)
                .orElseThrow(() -> ResourceNotFoundException.userByUsername(staffMember));

        if (!staff.isStaff()) {
            throw ForbiddenException.staffOnlyOperation("check-out");
        }

        if (!staff.isAccountNonLocked() || !staff.isEnabled()) {
            throw new ForbiddenException("Staff account is not active", "STAFF", staff.getRole().name());
        }
    }

    /**
     * Get and validate reservation for check-out
     */
    private Reservation getAndValidateReservation(String reservationId) {
        // Try by ID first, then by confirmation number
        Reservation reservation = reservationRepository.findById(reservationId)
                .or(() -> reservationRepository.findByConfirmationNumber(reservationId))
                .orElseThrow(() -> ResourceNotFoundException.reservation(reservationId));

        return reservation;
    }

    /**
     * Validate reservation state for check-out
     */
    private void validateReservationForCheckOut(Reservation reservation) {
        if (!reservation.canCheckOut()) {
            throw InvalidReservationStateException.cannotCheckOut(
                    reservation.getId(),
                    reservation.getConfirmationNumber(),
                    reservation.getStatus());
        }

        if (reservation.getActualCheckOutTime() != null) {
            throw InvalidReservationStateException.alreadyCheckedOut(
                    reservation.getId(),
                    reservation.getConfirmationNumber());
        }
    }

    /**
     * Process additional charges during stay
     */
    private BigDecimal processAdditionalCharges(Reservation reservation, CheckOutDto checkOutRequest) {
        BigDecimal total = BigDecimal.ZERO;

        if (checkOutRequest.getAdditionalCharges() != null) {
            for (CheckOutDto.AdditionalChargeDto chargeDto : checkOutRequest.getAdditionalCharges()) {
                Reservation.AdditionalCharge charge = Reservation.AdditionalCharge.builder()
                        .description(chargeDto.getDescription())
                        .amount(chargeDto.getAmount())
                        .type(chargeDto.getType())
                        .quantity(chargeDto.getQuantity() != null ? chargeDto.getQuantity() : 1)
                        .unitPrice(chargeDto.getUnitPrice())
                        .taxRate(chargeDto.getTaxRate())
                        .chargedAt(chargeDto.getChargeDateTime() != null ? 
                                  chargeDto.getChargeDateTime() : LocalDateTime.now())
                        .chargedBy("seba4s") // Current staff member
                        .taxable(true)
                        .build();

                // Calculate tax if applicable
                if (charge.getTaxRate() != null) {
                    BigDecimal taxAmount = charge.getAmount().multiply(charge.getTaxRate());
                    charge.setTaxAmount(taxAmount);
                }

                reservation.addAdditionalCharge(charge);
                total = total.add(charge.getAmount());

                log.info("Added additional charge: {} - ${}", charge.getDescription(), charge.getAmount());
            }
        }

        return total;
    }

    /**
     * Build check-out details from request
     */
    private Reservation.CheckOutDetails buildCheckOutDetails(CheckOutDto request, BigDecimal additionalChargesTotal) {
        LocalDateTime now = LocalDateTime.of(2025, 11, 22, 4, 14, 31);
        
        return Reservation.CheckOutDetails.builder()
                .requestedCheckOutTime(request.getActualCheckOutTime() != null ? 
                                      request.getActualCheckOutTime() : now)
                .lateCheckout(isLateCheckOut(request.getActualCheckOutTime()))
                .lateCheckoutFee(calculateLateCheckOutFee(request.getActualCheckOutTime()))
                .roomCondition(request.getRoomCondition() != null ? request.getRoomCondition() : "GOOD")
                .damageReport(extractDamageReport(request.getMaintenanceIssues()))
                .missingItems(extractMissingItems(request.getItemsLeftBehind()))
                .depositRefund(calculateDepositRefund(additionalChargesTotal))
                .depositRefundMethod("ORIGINAL_PAYMENT_METHOD")
                .keyCardsReturned(List.of("KEY_RETURNED_1", "KEY_RETURNED_2"))
                .missingKeyCards(request.getKeyCardsReturned() != null ? 
                               Math.max(0, 2 - request.getKeyCardsReturned()) : 0)
                .overallRating(request.getGuestRating())
                .roomRating(request.getGuestRating())
                .serviceRating(request.getGuestRating())
                .feedback(request.getGuestFeedback())
                .wouldRecommend(request.getGuestRating() != null && request.getGuestRating() >= 4)
                .invoiceNumber(generateInvoiceNumber())
                .finalAmount(BigDecimal.ZERO) // Will be calculated
                .finalPaymentMethod("CREDIT_CARD")
                .staffNotes(request.getNotes())
                .itemsLeftBehind(parseItemsLeftBehind(request.getItemsLeftBehind()))
                .build();
    }

    /**
     * Update room status after check-out
     */
    private void updateRoomStatusAfterCheckOut(Room room, CheckOutDto checkOutRequest) {
        // Determine room status based on condition and issues
        if ("DAMAGED".equals(checkOutRequest.getRoomCondition()) || 
            (checkOutRequest.getMaintenanceIssues() != null && !checkOutRequest.getMaintenanceIssues().trim().isEmpty())) {
            room.setStatus(Room.RoomStatus.MAINTENANCE);
            log.info("Room {} marked for maintenance due to issues: {}", 
                    room.getRoomNumber(), checkOutRequest.getMaintenanceIssues());
        } else {
            room.markForCleaning();
        }

        // Set housekeeping priority
        String priority = checkOutRequest.getHousekeepingPriority() != null ? 
                         checkOutRequest.getHousekeepingPriority() : "STANDARD";
        
        // TODO: Notify housekeeping service
        // housekeepingService.scheduleRoomCleaning(room.getId(), priority);

        roomRepository.save(room);
    }

    /**
     * Calculate final amount including all charges
     */
    private BigDecimal calculateFinalAmount(Reservation reservation, BigDecimal additionalChargesTotal) {
        BigDecimal baseAmount = reservation.getTotalAmount();
        BigDecimal lateCheckOutFee = calculateLateCheckOutFee(LocalDateTime.now());
        BigDecimal missingKeyCardFee = BigDecimal.ZERO; // Calculate if needed
        
        return baseAmount.add(additionalChargesTotal).add(lateCheckOutFee).add(missingKeyCardFee);
    }

    /**
     * Enrich check-out response with additional details
     */
    private void enrichCheckOutResponse(CheckOutResponseDto response, CheckOutDto request, 
                                      String staffMember, BigDecimal additionalChargesTotal) {
        
        response.setProcessedBy(staffMember);
        response.setFinalAmount(response.getFinalAmount().add(additionalChargesTotal));
        
        // Set guest feedback if provided
        if (request.getGuestRating() != null || request.getGuestFeedback() != null) {
            CheckOutResponseDto.GuestFeedbackDto feedback = CheckOutResponseDto.GuestFeedbackDto.builder()
                    .overallRating(request.getGuestRating())
                    .roomRating(request.getGuestRating())
                    .serviceRating(request.getGuestRating())
                    .valueRating(request.getGuestRating())
                    .comments(request.getGuestFeedback())
                    .wouldRecommend(request.getGuestRating() != null && request.getGuestRating() >= 4)
                    .likelihoodToReturn(request.getGuestRating())
                    .providedAt(LocalDateTime.of(2025, 11, 22, 4, 14, 31))
                    .build();
            response.setGuestFeedback(feedback);
        }

        // Set items left behind if any
        if (request.getItemsLeftBehind() != null && !request.getItemsLeftBehind().trim().isEmpty()) {
            List<CheckOutResponseDto.LostItemDto> lostItems = List.of(
                    CheckOutResponseDto.LostItemDto.builder()
                            .description(request.getItemsLeftBehind())
                            .locationFound("Room " + response.getRoom().getRoomNumber())
                            .foundBy(staffMember)
                            .foundAt(LocalDateTime.of(2025, 11, 22, 4, 14, 31))
                            .guestContacted(false)
                            .claimed(false)
                            .build()
            );
            response.setLostItems(lostItems);
        }
    }

    /**
     * Helper methods for check-out calculations
     */
    private boolean isLateCheckOut(LocalDateTime checkOutTime) {
        if (checkOutTime == null) checkOutTime = LocalDateTime.now();
        LocalDateTime standardCheckOut = LocalDateTime.of(2025, 11, 22, 12, 0); // 12:00 PM
        return checkOutTime.isAfter(standardCheckOut);
    }

    private BigDecimal calculateLateCheckOutFee(LocalDateTime checkOutTime) {
        return isLateCheckOut(checkOutTime) ? BigDecimal.valueOf(50000) : BigDecimal.ZERO;
    }

    private BigDecimal calculateDepositRefund(BigDecimal additionalCharges) {
        BigDecimal standardDeposit = BigDecimal.valueOf(200000);
        BigDecimal refund = standardDeposit.subtract(additionalCharges);
        return refund.max(BigDecimal.ZERO);
    }

    private String generateInvoiceNumber() {
        return "INV-2025-" + String.format("%06d", System.currentTimeMillis() % 1000000);
    }

    private List<String> extractDamageReport(String maintenanceIssues) {
        if (maintenanceIssues == null || maintenanceIssues.trim().isEmpty()) {
            return List.of();
        }
        return List.of(maintenanceIssues.split(","))
                .stream().map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    private List<String> extractMissingItems(String itemsString) {
        if (itemsString == null || itemsString.trim().isEmpty()) {
            return List.of();
        }
        return List.of(itemsString.split(","))
                .stream().map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    private List<String> parseItemsLeftBehind(String itemsString) {
        return extractMissingItems(itemsString);
    }
}