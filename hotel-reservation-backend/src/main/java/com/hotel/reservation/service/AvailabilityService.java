package com.hotel.reservation.service;

import com.hotel.reservation.dto.request.AvailabilitySearchDto;
import com.hotel.reservation.dto.response.RoomAvailabilityDto;
import com.hotel.reservation.exception.BadRequestException;
import com.hotel.reservation.exception.ResourceNotFoundException;
import com.hotel.reservation.mapper.RoomMapper;
import com.hotel.reservation.model.Room;
import com.hotel.reservation.model.Reservation;
import com.hotel.reservation.repository.RoomRepository;
import com.hotel.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final RoomMapper roomMapper;

    /**
     * HU001: Search for available rooms based on criteria
     */
    @Transactional(readOnly = true)
    public List<RoomAvailabilityDto> searchAvailableRooms(AvailabilitySearchDto searchRequest) {
        log.info("Searching available rooms - Check-in: {}, Check-out: {}, Adults: {}, Children: {}", 
                searchRequest.getCheckInDate(), searchRequest.getCheckOutDate(), 
                searchRequest.getAdults(), searchRequest.getChildren());

        // Validate search parameters
        validateSearchRequest(searchRequest);

        // Get base available rooms
        List<Room> candidateRooms = getCandidateRooms(searchRequest);
        
        if (candidateRooms.isEmpty()) {
            log.info("No rooms match the basic criteria");
            return List.of();
        }

        // Filter rooms by actual availability (no conflicting reservations)
        List<Room> availableRooms = filterAvailableRooms(candidateRooms, 
                searchRequest.getCheckInDate(), searchRequest.getCheckOutDate());

        if (availableRooms.isEmpty()) {
            log.info("No rooms available for the requested dates");
            return List.of();
        }

        // Convert to DTOs with pricing and availability info
        List<RoomAvailabilityDto> results = availableRooms.stream()
                .map(room -> mapRoomToAvailabilityDto(room, searchRequest))
                .collect(Collectors.toList());

        // Apply business rules and filters
        results = applyBusinessRulesAndFilters(results, searchRequest);

        // Sort results (by price, rating, etc.)
        results = sortResults(results);

        log.info("Found {} available rooms for the search criteria", results.size());
        return results;
    }

    /**
     * Get room details with pricing for specific dates
     */
    @Transactional(readOnly = true)
    public RoomAvailabilityDto getRoomDetails(String roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        log.info("Getting room details for roomId: {} with dates {} to {}", roomId, checkInDate, checkOutDate);

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> ResourceNotFoundException.room(roomId));

        if (!room.getActive()) {
            throw new BadRequestException("Room is not active", "roomId", roomId);
        }

        boolean isAvailable = true;
        if (checkInDate != null && checkOutDate != null) {
            isAvailable = checkRoomAvailability(roomId, checkInDate, checkOutDate);
        }

        RoomAvailabilityDto dto = roomMapper.toAvailabilityDtoWithDates(room, checkInDate, checkOutDate, isAvailable);
        
        // Apply seasonal pricing if dates are provided
        if (checkInDate != null && checkOutDate != null) {
            dto = calculatePricingForDates(dto, room, checkInDate, checkOutDate);
        }

        return dto;
    }

    /**
     * Check if a specific room is available for given dates
     */
    @Transactional(readOnly = true)
    public boolean checkRoomAvailability(String roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        log.debug("Checking availability for room {} from {} to {}", roomId, checkInDate, checkOutDate);

        // Check if room exists and is active
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> ResourceNotFoundException.room(roomId));

        if (!room.getActive() || !room.isAvailable()) {
            return false;
        }

        // Check for conflicting reservations
        List<Reservation> conflicts = reservationRepository.findConflictingReservations(
                roomId, checkInDate, checkOutDate);

        return conflicts.isEmpty();
    }

    /**
     * Validate search request parameters
     */
    private void validateSearchRequest(AvailabilitySearchDto searchRequest) {
        LocalDate now = LocalDate.now();

        if (searchRequest.getCheckInDate().isBefore(now)) {
            throw BadRequestException.pastDate("checkInDate", searchRequest.getCheckInDate().toString());
        }

        if (searchRequest.getCheckOutDate().isBefore(searchRequest.getCheckInDate()) ||
            searchRequest.getCheckOutDate().equals(searchRequest.getCheckInDate())) {
            throw BadRequestException.invalidDateRange(
                searchRequest.getCheckInDate().toString(),
                searchRequest.getCheckOutDate().toString());
        }

        long nights = ChronoUnit.DAYS.between(searchRequest.getCheckInDate(), searchRequest.getCheckOutDate());
        if (nights > 30) {
            throw BadRequestException.exceedsMaxStayDuration(nights, 30L);
        }

        if (searchRequest.getTotalGuests() > 10) {
            throw BadRequestException.invalidGuestCount(searchRequest.getAdults(), searchRequest.getChildren());
        }
    }

    /**
     * Get candidate rooms based on basic criteria
     */
    private List<Room> getCandidateRooms(AvailabilitySearchDto searchRequest) {
        Integer totalGuests = searchRequest.getTotalGuests();
        String roomType = searchRequest.getRoomType();

        List<Room> rooms;

        if (roomType != null && !roomType.trim().isEmpty()) {
            // Search by type and capacity
            rooms = roomRepository.findAvailableRoomsByTypeAndCapacity(roomType, totalGuests);
        } else {
            // Search by capacity only
            rooms = roomRepository.findAvailableRoomsByMinCapacity(totalGuests);
        }

        // Apply price range filter if specified
        if (searchRequest.getPriceRange() != null) {
            rooms = filterByPriceRange(rooms, searchRequest.getPriceRange());
        }

        // Apply amenity filter if specified
        if (searchRequest.getAmenities() != null && !searchRequest.getAmenities().trim().isEmpty()) {
            List<String> requiredAmenities = parseAmenities(searchRequest.getAmenities());
            rooms = rooms.stream()
                    .filter(room -> room.getAmenities() != null && 
                                   room.getAmenities().containsAll(requiredAmenities))
                    .collect(Collectors.toList());
        }

        // Apply accessibility filter if needed
        if (Boolean.TRUE.equals(searchRequest.getAccessibilityRequired())) {
            rooms = rooms.stream()
                    .filter(room -> Boolean.TRUE.equals(room.getAccessible()))
                    .collect(Collectors.toList());
        }

        return rooms;
    }

    /**
     * Filter rooms by actual availability (no booking conflicts)
     */
    private List<Room> filterAvailableRooms(List<Room> candidateRooms, LocalDate checkIn, LocalDate checkOut) {
        return candidateRooms.stream()
                .filter(room -> {
                    List<Reservation> conflicts = reservationRepository.findConflictingReservations(
                            room.getId(), checkIn, checkOut);
                    return conflicts.isEmpty();
                })
                .collect(Collectors.toList());
    }

    /**
     * Map Room to RoomAvailabilityDto with search context
     */
    private RoomAvailabilityDto mapRoomToAvailabilityDto(Room room, AvailabilitySearchDto searchRequest) {
        RoomAvailabilityDto dto = roomMapper.toAvailabilityDtoWithDates(
                room, 
                searchRequest.getCheckInDate(), 
                searchRequest.getCheckOutDate(), 
                true // Already filtered for availability
        );

        // Calculate pricing for the stay
        dto = calculatePricingForDates(dto, room, searchRequest.getCheckInDate(), searchRequest.getCheckOutDate());

        return dto;
    }

    /**
     * Calculate pricing for specific dates
     */
    private RoomAvailabilityDto calculatePricingForDates(RoomAvailabilityDto dto, Room room, 
                                                        LocalDate checkIn, LocalDate checkOut) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        dto.setNights(nights);

        // Calculate base total
        BigDecimal total = room.getBasePrice().multiply(BigDecimal.valueOf(nights));

        // Apply seasonal pricing if configured
        if (room.getSeasonalPricing() != null && !room.getSeasonalPricing().isEmpty()) {
            total = applySeasonalPricing(total, room, checkIn, checkOut);
        }

        dto.setTotalPrice(total);

        // Check for early bird discounts
        if (checkIn.isAfter(LocalDate.now().plusDays(30))) {
            BigDecimal discount = total.multiply(BigDecimal.valueOf(0.15)); // 15% discount
            dto.setTotalPrice(total.subtract(discount));
            
            // Add offer information
            RoomAvailabilityDto.OfferDto earlyBird = RoomAvailabilityDto.OfferDto.builder()
                    .name("Early Bird Discount")
                    .discountPercent(15)
                    .discountAmount(discount)
                    .description("Book 30 days in advance and save 15%")
                    .validUntil(LocalDate.now().plusDays(7))
                    .build();
            dto.setOffers(List.of(earlyBird));
        }

        return dto;
    }

    /**
     * Apply seasonal pricing adjustments
     */
    private BigDecimal applySeasonalPricing(BigDecimal baseTotal, Room room, LocalDate checkIn, LocalDate checkOut) {
        // Simplified seasonal pricing logic
        // In real implementation, would iterate through each night and apply applicable pricing
        
        for (Room.SeasonalPricing seasonal : room.getSeasonalPricing()) {
            if (!checkIn.isBefore(seasonal.getStartDate()) && !checkOut.isAfter(seasonal.getEndDate())) {
                if ("PERCENTAGE".equals(seasonal.getModifierType())) {
                    return baseTotal.multiply(seasonal.getPriceModifier());
                } else if ("FIXED_AMOUNT".equals(seasonal.getModifierType())) {
                    long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
                    return baseTotal.add(seasonal.getPriceModifier().multiply(BigDecimal.valueOf(nights)));
                }
            }
        }
        
        return baseTotal;
    }

    /**
     * Filter rooms by price range
     */
    private List<Room> filterByPriceRange(List<Room> rooms, String priceRange) {
        Map<String, BigDecimal[]> priceRanges = Map.of(
            "LOW", new BigDecimal[]{BigDecimal.ZERO, BigDecimal.valueOf(300000)},
            "MEDIUM", new BigDecimal[]{BigDecimal.valueOf(300000), BigDecimal.valueOf(600000)},
            "HIGH", new BigDecimal[]{BigDecimal.valueOf(600000), BigDecimal.valueOf(1000000)},
            "LUXURY", new BigDecimal[]{BigDecimal.valueOf(1000000), BigDecimal.valueOf(999999999)}
        );

        BigDecimal[] range = priceRanges.get(priceRange.toUpperCase());
        if (range == null) return rooms;

        return rooms.stream()
                .filter(room -> room.getBasePrice().compareTo(range[0]) >= 0 && 
                               room.getBasePrice().compareTo(range[1]) <= 0)
                .collect(Collectors.toList());
    }

    /**
     * Parse amenities string into list
     */
    private List<String> parseAmenities(String amenitiesString) {
        return List.of(amenitiesString.split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Apply additional business rules and filters
     */
    private List<RoomAvailabilityDto> applyBusinessRulesAndFilters(List<RoomAvailabilityDto> rooms, 
                                                                  AvailabilitySearchDto searchRequest) {
        // Apply any additional business logic here
        // For example: VIP customer preferences, corporate rates, etc.
        
        return rooms.stream()
                .filter(room -> room.getAvailable()) // Double-check availability
                .collect(Collectors.toList());
    }

    /**
     * Sort results by business criteria
     */
    private List<RoomAvailabilityDto> sortResults(List<RoomAvailabilityDto> rooms) {
        // Sort by price (ascending) as default
        return rooms.stream()
                .sorted((r1, r2) -> {
                    if (r1.getTotalPrice() == null && r2.getTotalPrice() == null) return 0;
                    if (r1.getTotalPrice() == null) return 1;
                    if (r2.getTotalPrice() == null) return -1;
                    return r1.getTotalPrice().compareTo(r2.getTotalPrice());
                })
                .collect(Collectors.toList());
    }
}