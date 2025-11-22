package com.hotel.reservation.controller;

import com.hotel.reservation.dto.request.CheckInDto;
import com.hotel.reservation.dto.response.CheckInResponseDto;
import com.hotel.reservation.dto.response.ReservationResponseDto;
import com.hotel.reservation.service.CheckInService;
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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/checkin")
@RequiredArgsConstructor
@Tag(name = "Check-In", description = "Check-in management APIs - HU009")
@SecurityRequirement(name = "Bearer Authentication")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CheckInController {

    private static final Logger logger = LoggerFactory.getLogger(CheckInController.class);

    private final CheckInService checkInService;

    @Operation(
        summary = "Process check-in",
        description = "HU009: Process guest check-in with identity verification"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Check-in processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid check-in data"),
        @ApiResponse(responseCode = "404", description = "Reservation not found"),
        @ApiResponse(responseCode = "409", description = "Cannot process check-in for current reservation state"),
        @ApiResponse(responseCode = "403", description = "Staff access required")
    })
    @PostMapping("/process")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<CheckInResponseDto> processCheckIn(
            @Valid @RequestBody CheckInDto checkInRequest,
            Authentication authentication) {

        String staffMember = authentication.getName();
        logger.info("Processing check-in - Staff: {}, Reservation: {}, Document: {} {}",
                staffMember, checkInRequest.getReservationId(),
                checkInRequest.getDocumentType(), checkInRequest.getDocumentNumber());

        try {
            CheckInResponseDto checkInResponse = checkInService.processCheckIn(checkInRequest, staffMember);

            logger.info("Check-in processed successfully - Reservation: {}, Room: {}",
                    checkInResponse.getReservationId(), checkInResponse.getRoom().getRoomNumber());

            return ResponseEntity.ok(checkInResponse);

        } catch (Exception e) {
            logger.error("Error processing check-in for reservation {}: {}",
                    checkInRequest.getReservationId(), e.getMessage());
            throw e;
        }
    }

    @Operation(
        summary = "Get today's check-ins",
        description = "Get list of reservations scheduled for check-in today"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Check-ins retrieved"),
        @ApiResponse(responseCode = "403", description = "Staff access required")
    })
    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<List<ReservationResponseDto>> getTodaysCheckIns() {

        logger.info("Getting today's check-ins for date: {}", LocalDate.now());

        try {
            List<ReservationResponseDto> todaysCheckIns = checkInService.getTodaysCheckIns();

            logger.info("Found {} reservations for check-in today", todaysCheckIns.size());
            return ResponseEntity.ok(todaysCheckIns);

        } catch (Exception e) {
            logger.error("Error getting today's check-ins: {}", e.getMessage());
            throw e;
        }
    }

    @Operation(
        summary = "Get check-ins by date",
        description = "Get list of reservations scheduled for check-in on specific date"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Check-ins retrieved"),
        @ApiResponse(responseCode = "400", description = "Invalid date"),
        @ApiResponse(responseCode = "403", description = "Staff access required")
    })
    @GetMapping("/date/{date}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<List<ReservationResponseDto>> getCheckInsByDate(
            @Parameter(description = "Check-in date (YYYY-MM-DD)", required = true)
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        logger.info("Getting check-ins for date: {}", date);

        try {
            List<ReservationResponseDto> checkIns = checkInService.getCheckInsByDate(date);

            logger.info("Found {} reservations for check-in on {}", checkIns.size(), date);
            return ResponseEntity.ok(checkIns);

        } catch (Exception e) {
            logger.error("Error getting check-ins for date {}: {}", date, e.getMessage());
            throw e;
        }
    }

    @Operation(
        summary = "Validate reservation for check-in",
        description = "Validate if a reservation can be checked in"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Validation result returned"),
        @ApiResponse(responseCode = "404", description = "Reservation not found"),
        @ApiResponse(responseCode = "403", description = "Staff access required")
    })
    @GetMapping("/validate/{confirmationNumber}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ReservationResponseDto> validateReservationForCheckIn(
            @Parameter(description = "Reservation confirmation number", required = true)
            @PathVariable String confirmationNumber) {

        logger.info("Validating reservation for check-in: {}", confirmationNumber);

        try {
            ReservationResponseDto reservation = checkInService.validateReservationForCheckIn(confirmationNumber);

            logger.info("Reservation {} is valid for check-in", confirmationNumber);
            return ResponseEntity.ok(reservation);

        } catch (Exception e) {
            logger.error("Error validating reservation {} for check-in: {}", confirmationNumber, e.getMessage());
            throw e;
        }
    }

    @Operation(
        summary = "Search reservation by guest info",
        description = "Search reservation using guest information for check-in"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reservations found"),
        @ApiResponse(responseCode = "404", description = "No reservations found"),
        @ApiResponse(responseCode = "403", description = "Staff access required")
    })
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<List<ReservationResponseDto>> searchReservationForCheckIn(
            @Parameter(description = "Guest email")
            @RequestParam(required = false) String email,

            @Parameter(description = "Guest last name")
            @RequestParam(required = false) String lastName,

            @Parameter(description = "Document number")
            @RequestParam(required = false) String documentNumber) {

        logger.info("Searching reservations for check-in - Email: {}, LastName: {}, Document: {}",
                email, lastName, documentNumber);

        try {
            List<ReservationResponseDto> reservations =
                    checkInService.searchReservationsForCheckIn(email, lastName, documentNumber);

            logger.info("Found {} reservations matching search criteria", reservations.size());
            return ResponseEntity.ok(reservations);

        } catch (Exception e) {
            logger.error("Error searching reservations for check-in: {}", e.getMessage());
            throw e;
        }
    }
}
