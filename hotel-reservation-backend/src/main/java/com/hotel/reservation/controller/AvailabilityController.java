package com.hotel.reservation.controller;

import com.hotel.reservation.dto.request.AvailabilitySearchDto;
import com.hotel.reservation.dto.response.RoomAvailabilityDto;
import com.hotel.reservation.service.AvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
@Tag(name = "Room Availability", description = "Room availability management APIs")
public class AvailabilityController {

    private static final Logger logger = LoggerFactory.getLogger(AvailabilityController.class);

    private final AvailabilityService availabilityService;

    @Operation(
        summary = "Search available rooms", 
        description = "HU001: Search for available rooms based on check-in/check-out dates and guest count"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Available rooms found"),
        @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
        @ApiResponse(responseCode = "404", description = "No rooms available for the specified criteria")
    })
    @PostMapping("/search")
    public ResponseEntity<List<RoomAvailabilityDto>> searchAvailableRooms(
            @Valid @RequestBody AvailabilitySearchDto searchRequest) {
        
        logger.info("Room availability search - Check-in: {}, Check-out: {}, Adults: {}, Children: {}", 
                   searchRequest.getCheckInDate(), searchRequest.getCheckOutDate(), 
                   searchRequest.getAdults(), searchRequest.getChildren());

        try {
            List<RoomAvailabilityDto> availableRooms = availabilityService.searchAvailableRooms(searchRequest);
            
            logger.info("Found {} available rooms for the search criteria", availableRooms.size());
            return ResponseEntity.ok(availableRooms);
            
        } catch (Exception e) {
            logger.error("Error searching available rooms: {}", e.getMessage());
            throw e;
        }
    }

    @Operation(
        summary = "Quick availability check", 
        description = "Quick check for room availability using query parameters"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Availability check completed"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    @GetMapping("/quick-search")
    public ResponseEntity<List<RoomAvailabilityDto>> quickAvailabilityCheck(
            @Parameter(description = "Check-in date (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            
            @Parameter(description = "Check-out date (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            
            @Parameter(description = "Number of adults", required = true)
            @RequestParam Integer adults,
            
            @Parameter(description = "Number of children", required = false)
            @RequestParam(defaultValue = "0") Integer children,
            
            @Parameter(description = "Room type filter", required = false)
            @RequestParam(required = false) String roomType) {

        logger.info("Quick availability check - Check-in: {}, Check-out: {}, Adults: {}, Children: {}, RoomType: {}", 
                   checkInDate, checkOutDate, adults, children, roomType);

        AvailabilitySearchDto searchRequest = AvailabilitySearchDto.builder()
                .checkInDate(checkInDate)
                .checkOutDate(checkOutDate)
                .adults(adults)
                .children(children)
                .roomType(roomType)
                .build();

        try {
            List<RoomAvailabilityDto> availableRooms = availabilityService.searchAvailableRooms(searchRequest);
            
            logger.info("Quick search found {} available rooms", availableRooms.size());
            return ResponseEntity.ok(availableRooms);
            
        } catch (Exception e) {
            logger.error("Error in quick availability check: {}", e.getMessage());
            throw e;
        }
    }

    @Operation(
        summary = "Get room details", 
        description = "Get detailed information about a specific room"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Room details retrieved"),
        @ApiResponse(responseCode = "404", description = "Room not found")
    })
    @GetMapping("/room/{roomId}")
    public ResponseEntity<RoomAvailabilityDto> getRoomDetails(
            @Parameter(description = "Room ID", required = true)
            @PathVariable String roomId,
            
            @Parameter(description = "Check-in date for pricing", required = false)
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            
            @Parameter(description = "Check-out date for pricing", required = false)
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate) {

        logger.info("Getting room details for roomId: {}", roomId);

        try {
            RoomAvailabilityDto roomDetails = availabilityService.getRoomDetails(roomId, checkInDate, checkOutDate);
            
            logger.info("Room details retrieved for roomId: {}", roomId);
            return ResponseEntity.ok(roomDetails);
            
        } catch (Exception e) {
            logger.error("Error getting room details for roomId {}: {}", roomId, e.getMessage());
            throw e;
        }
    }

    @Operation(
        summary = "Check specific room availability", 
        description = "Check if a specific room is available for given dates"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Availability status returned"),
        @ApiResponse(responseCode = "404", description = "Room not found")
    })
    @GetMapping("/room/{roomId}/check")
    public ResponseEntity<Boolean> checkRoomAvailability(
            @Parameter(description = "Room ID", required = true)
            @PathVariable String roomId,
            
            @Parameter(description = "Check-in date", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            
            @Parameter(description = "Check-out date", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate) {

        logger.info("Checking availability for room {} from {} to {}", roomId, checkInDate, checkOutDate);

        try {
            boolean isAvailable = availabilityService.checkRoomAvailability(roomId, checkInDate, checkOutDate);
            
            logger.info("Room {} availability: {}", roomId, isAvailable);
            return ResponseEntity.ok(isAvailable);
            
        } catch (Exception e) {
            logger.error("Error checking room availability for roomId {}: {}", roomId, e.getMessage());
            throw e;
        }
    }
}