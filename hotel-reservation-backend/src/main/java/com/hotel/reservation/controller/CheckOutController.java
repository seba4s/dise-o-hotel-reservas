package com.hotel.reservation.controller;

import com.hotel.reservation.dto.request.CheckOutDto;
import com.hotel.reservation.dto.response.CheckOutResponseDto;
import com.hotel.reservation.dto.response.ReservationResponseDto;
import com.hotel.reservation.service.CheckOutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
@Tag(name = "Check-Out", description = "Check-out management APIs - HU010")
@SecurityRequirement(name = "Bearer Authentication")
public class CheckOutController {

    private static final Logger logger = LoggerFactory.getLogger(CheckOutController.class);

    private final CheckOutService checkOutService;

    @Operation(
        summary = "Process check-out", 
        description = "HU010: Process guest check-out with additional charges and room status update"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Check-out processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid check-out data"),
        @ApiResponse(responseCode = "404", description = "Reservation not found"),
        @ApiResponse(responseCode = "409", description = "Cannot process check-out for current reservation state"),
        @ApiResponse(responseCode = "403", description = "Staff access required")
    })
    @PostMapping("/process")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<CheckOutResponseDto> processCheckOut(
            @Valid @RequestBody CheckOutDto checkOutRequest,
            Authentication authentication) {

        String staffMember = authentication.getName();
        logger.info("Processing check-out - Staff: {}, Reservation: {}, Additional charges: {}", 
                   staffMember, checkOutRequest.getReservationId(), 
                   checkOutRequest.getAdditionalCharges() != null ? checkOutRequest.getAdditionalCharges().size() : 0);

        try {
            CheckOutResponseDto checkOutResponse = checkOutService.processCheckOut(checkOutRequest, staffMember);
            
            logger.info("Check-out processed successfully - Reservation: {}, Final amount: {}", 
                       checkOutResponse.getReservationId(), checkOutResponse.getFinalAmount());
            
            return ResponseEntity.ok(checkOutResponse);
            
        } catch (Exception e) {
            logger.error("Error processing check-out for reservation {}: {}", 
                        checkOutRequest.getReservationId(), e.getMessage());
            throw e;
        }
    }

    @Operation(
        summary = "Get today's check-outs", 
        description = "Get list of reservations scheduled for check-out today"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Check-outs retrieved"),
        @ApiResponse(responseCode = "403", description = "Staff access required")
    })
    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<List<ReservationResponseDto>> getTodaysCheckOuts() {

        logger.info("Getting today's check-outs for date: {}", LocalDate.now());

        try {
            List<ReservationResponseDto> todaysCheckOuts = checkOutService.getTodaysCheckOuts();
            
            logger.info("Found {} reservations for check-out today", todaysCheckOuts.size());
            return ResponseEntity.ok(todaysCheckOuts);
            
        } catch (Exception e) {
            logger.error("Error getting today's check-outs: {}", e.getMessage());
            throw e;
        }
    }

    @Operation(
        summary = "Get check-outs by date", 
        description = "Get list of reservations scheduled for check-out on specific date"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Check-outs retrieved"),
        @ApiResponse(responseCode = "400", description = "Invalid date"),
        @ApiResponse(responseCode = "403", description = "Staff access required")
    })
    @GetMapping("/date/{date}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<List<ReservationResponseDto>> getCheckOutsByDate(
            @Parameter(description = "Check-out date (YYYY-MM-DD)", required = true)
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        logger.info("Getting check-outs for date: {}", date);

        try {
            List<ReservationResponseDto> checkOuts = checkOutService.getCheckOutsByDate(date);
            
            logger.info("Found {} reservations for check-out on {}", checkOuts.size(), date);
            return ResponseEntity.ok(checkOuts);
            
        } catch (Exception e) {
            logger.error("Error getting check-outs for date {}: {}", date, e.getMessage());
            throw e;
        }
    }

    @Operation(
        summary = "Get current guest info", 
        description = "Get information about current guest in a room for check-out"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Guest info retrieved"),
        @ApiResponse(responseCode = "404", description = "No guest found in room"),
        @ApiResponse(responseCode = "403", description = "Staff access required")
    })
    @GetMapping("/room/{roomNumber}/guest")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ReservationResponseDto> getCurrentGuestInRoom(
            @Parameter(description = "Room number", required = true)
            @PathVariable String roomNumber) {

        logger.info("Getting current guest info for room: {}", roomNumber);

        try {
            ReservationResponseDto guestInfo = checkOutService.getCurrentGuestInRoom(roomNumber);
            
            logger.info("Found guest in room {}: {}", roomNumber, guestInfo.getGuest().getEmail());
            return ResponseEntity.ok(guestInfo);
            
        } catch (Exception e) {
            logger.error("Error getting guest info for room {}: {}", roomNumber, e.getMessage());
            throw e;
        }
    }

    @Operation(
        summary = "Validate reservation for check-out", 
        description = "Validate if a reservation can be checked out"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Validation result returned"),
        @ApiResponse(responseCode = "404", description = "Reservation not found"),
        @ApiResponse(responseCode = "403", description = "Staff access required")
    })
    @GetMapping("/validate/{confirmationNumber}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ReservationResponseDto> validateReservationForCheckOut(
            @Parameter(description = "Reservation confirmation number", required = true)
            @PathVariable String confirmationNumber) {

        logger.info("Validating reservation for check-out: {}", confirmationNumber);

        try {
            ReservationResponseDto reservation = checkOutService.validateReservationForCheckOut(confirmationNumber);
            
            logger.info("Reservation {} is valid for check-out", confirmationNumber);
            return ResponseEntity.ok(reservation);
            
        } catch (Exception e) {
            logger.error("Error validating reservation {} for check-out: {}", confirmationNumber, e.getMessage());
            throw e;
        }
    }

    @Operation(
        summary = "Get check-out history", 
        description = "Get history of processed check-outs"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Check-out history retrieved"),
        @ApiResponse(responseCode = "403", description = "Staff access required")
    })
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<List<CheckOutResponseDto>> getCheckOutHistory(
            @Parameter(description = "Number of days back to search", required = false)
            @RequestParam(defaultValue = "7") Integer days) {

        logger.info("Getting check-out history for last {} days", days);

        try {
            List<CheckOutResponseDto> checkOutHistory = checkOutService.getCheckOutHistory(days);
            
            logger.info("Retrieved {} check-out records from history", checkOutHistory.size());
            return ResponseEntity.ok(checkOutHistory);
            
        } catch (Exception e) {
            logger.error("Error getting check-out history: {}", e.getMessage());
            throw e;
        }
    }

    @Operation(
        summary = "Calculate additional charges", 
        description = "Preview total amount with additional charges before check-out"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Calculation completed"),
        @ApiResponse(responseCode = "404", description = "Reservation not found"),
        @ApiResponse(responseCode = "403", description = "Staff access required")
    })
    @PostMapping("/calculate/{reservationId}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<CheckOutResponseDto> calculateCheckOutTotal(
            @Parameter(description = "Reservation ID", required = true)
            @PathVariable String reservationId,
            @RequestBody(required = false) List<CheckOutDto.AdditionalChargeDto> additionalCharges) {

        logger.info("Calculating check-out total for reservation: {} with {} additional charges", 
                   reservationId, additionalCharges != null ? additionalCharges.size() : 0);

        try {
            CheckOutResponseDto calculation = checkOutService.calculateCheckOutTotal(reservationId, additionalCharges);
            
            logger.info("Check-out calculation completed for reservation: {}, total: {}", 
                       reservationId, calculation.getFinalAmount());
            
            return ResponseEntity.ok(calculation);
            
        } catch (Exception e) {
            logger.error("Error calculating check-out total for reservation {}: {}", reservationId, e.getMessage());
            throw e;
        }
    }
}