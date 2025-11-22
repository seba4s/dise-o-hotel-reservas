package com.hotel.reservation.controller;

import com.hotel.reservation.dto.request.CreateReservationDto;
import com.hotel.reservation.dto.response.ReservationResponseDto;
import com.hotel.reservation.service.ReservationService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations", description = "Reservation management APIs - HU004")
@SecurityRequirement(name = "Bearer Authentication")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReservationController {

    private static final Logger logger = LoggerFactory.getLogger(ReservationController.class);

    private final ReservationService reservationService;

    @Operation(
        summary = "Create new reservation", 
        description = "HU004: Create a new room reservation (pre-reservation state)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Reservation created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid reservation data"),
        @ApiResponse(responseCode = "409", description = "Room not available for selected dates"),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'STAFF', 'ADMIN')")
    public ResponseEntity<ReservationResponseDto> createReservation(
            @Valid @RequestBody CreateReservationDto reservationRequest,
            Authentication authentication) {

        String currentUser = authentication.getName();
        logger.info("Creating reservation for user: {} - Room: {}, Dates: {} to {}", 
                   currentUser, reservationRequest.getRoomId(), 
                   reservationRequest.getCheckInDate(), reservationRequest.getCheckOutDate());

        try {
            ReservationResponseDto reservation = reservationService.createReservation(reservationRequest, currentUser);
            
            logger.info("Reservation created successfully - ID: {}, Confirmation: {}", 
                       reservation.getId(), reservation.getConfirmationNumber());
            
            return new ResponseEntity<>(reservation, HttpStatus.CREATED);
            
        } catch (Exception e) {
            logger.error("Error creating reservation for user {}: {}", currentUser, e.getMessage());
            throw e;
        }
    }

    @Operation(
        summary = "Get reservation by confirmation number", 
        description = "Retrieve reservation details using confirmation number"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reservation found"),
        @ApiResponse(responseCode = "404", description = "Reservation not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/confirmation/{confirmationNumber}")
    @PreAuthorize("hasAnyRole('CLIENT', 'STAFF', 'ADMIN')")
    public ResponseEntity<ReservationResponseDto> getReservationByConfirmation(
            @Parameter(description = "Reservation confirmation number", required = true)
            @PathVariable String confirmationNumber,
            Authentication authentication) {

        String currentUser = authentication.getName();
        logger.info("Getting reservation by confirmation: {} for user: {}", confirmationNumber, currentUser);

        try {
            ReservationResponseDto reservation = reservationService.getReservationByConfirmation(
                confirmationNumber, currentUser);
            
            logger.info("Reservation found: {}", confirmationNumber);
            return ResponseEntity.ok(reservation);
            
        } catch (Exception e) {
            logger.error("Error getting reservation {}: {}", confirmationNumber, e.getMessage());
            throw e;
        }
    }

    @Operation(
        summary = "Get reservation by ID", 
        description = "Retrieve reservation details using reservation ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reservation found"),
        @ApiResponse(responseCode = "404", description = "Reservation not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{reservationId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'STAFF', 'ADMIN')")
    public ResponseEntity<ReservationResponseDto> getReservationById(
            @Parameter(description = "Reservation ID", required = true)
            @PathVariable String reservationId,
            Authentication authentication) {

        String currentUser = authentication.getName();
        logger.info("Getting reservation by ID: {} for user: {}", reservationId, currentUser);

        try {
            ReservationResponseDto reservation = reservationService.getReservationById(reservationId, currentUser);
            
            logger.info("Reservation found: {}", reservationId);
            return ResponseEntity.ok(reservation);
            
        } catch (Exception e) {
            logger.error("Error getting reservation {}: {}", reservationId, e.getMessage());
            throw e;
        }
    }

    @Operation(
        summary = "Get user reservations", 
        description = "Get all reservations for the authenticated user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reservations retrieved"),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping("/my-reservations")
    @PreAuthorize("hasAnyRole('CLIENT', 'STAFF', 'ADMIN')")
    public ResponseEntity<List<ReservationResponseDto>> getMyReservations(Authentication authentication) {

        String currentUser = authentication.getName();
        logger.info("Getting reservations for user: {}", currentUser);

        try {
            List<ReservationResponseDto> reservations = reservationService.getUserReservations(currentUser);
            
            logger.info("Found {} reservations for user: {}", reservations.size(), currentUser);
            return ResponseEntity.ok(reservations);
            
        } catch (Exception e) {
            logger.error("Error getting reservations for user {}: {}", currentUser, e.getMessage());
            throw e;
        }
    }

    @Operation(
        summary = "Update reservation", 
        description = "Update an existing reservation (only in pre-reservation state)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reservation updated"),
        @ApiResponse(responseCode = "400", description = "Invalid update data"),
        @ApiResponse(responseCode = "404", description = "Reservation not found"),
        @ApiResponse(responseCode = "409", description = "Cannot update reservation in current state")
    })
    @PutMapping("/{reservationId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'STAFF', 'ADMIN')")
    public ResponseEntity<ReservationResponseDto> updateReservation(
            @Parameter(description = "Reservation ID", required = true)
            @PathVariable String reservationId,
            @Valid @RequestBody CreateReservationDto updateRequest,
            Authentication authentication) {

        String currentUser = authentication.getName();
        logger.info("Updating reservation: {} for user: {}", reservationId, currentUser);

        try {
            ReservationResponseDto updatedReservation = reservationService.updateReservation(
                reservationId, updateRequest, currentUser);
            
            logger.info("Reservation updated successfully: {}", reservationId);
            return ResponseEntity.ok(updatedReservation);
            
        } catch (Exception e) {
            logger.error("Error updating reservation {}: {}", reservationId, e.getMessage());
            throw e;
        }
    }

    @Operation(
        summary = "Cancel reservation", 
        description = "Cancel an existing reservation"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reservation cancelled"),
        @ApiResponse(responseCode = "404", description = "Reservation not found"),
        @ApiResponse(responseCode = "409", description = "Cannot cancel reservation in current state")
    })
    @DeleteMapping("/{reservationId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'STAFF', 'ADMIN')")
    public ResponseEntity<ReservationResponseDto> cancelReservation(
            @Parameter(description = "Reservation ID", required = true)
            @PathVariable String reservationId,
            Authentication authentication) {

        String currentUser = authentication.getName();
        logger.info("Cancelling reservation: {} for user: {}", reservationId, currentUser);

        try {
            ReservationResponseDto cancelledReservation = reservationService.cancelReservation(reservationId, currentUser);
            
            logger.info("Reservation cancelled successfully: {}", reservationId);
            return ResponseEntity.ok(cancelledReservation);
            
        } catch (Exception e) {
            logger.error("Error cancelling reservation {}: {}", reservationId, e.getMessage());
            throw e;
        }
    }
}