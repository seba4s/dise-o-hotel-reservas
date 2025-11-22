package com.hotel.reservation.exception;

import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Exception thrown when a room is not available for booking
 * Used specifically for availability conflicts in HU001 and HU004
 */
@Getter
public class RoomNotAvailableException extends RuntimeException {

    private final String roomId;
    private final String roomNumber;
    private final String conflictPeriod;
    private final List<String> conflictingReservations;
    private final Map<String, Object> alternativeSuggestions;

    public RoomNotAvailableException(String message) {
        super(message);
        this.roomId = null;
        this.roomNumber = null;
        this.conflictPeriod = null;
        this.conflictingReservations = null;
        this.alternativeSuggestions = null;
    }

    public RoomNotAvailableException(String message, String roomId, String roomNumber,
                                   String conflictPeriod) {
        super(message);
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.conflictPeriod = conflictPeriod;
        this.conflictingReservations = null;
        this.alternativeSuggestions = null;
    }

    public RoomNotAvailableException(String message, String roomId, String roomNumber,
                                   String conflictPeriod, List<String> conflictingReservations,
                                   Map<String, Object> alternativeSuggestions) {
        super(message);
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.conflictPeriod = conflictPeriod;
        this.conflictingReservations = conflictingReservations;
        this.alternativeSuggestions = alternativeSuggestions;
    }

    /**
     * Static factory methods for specific availability scenarios
     */
    public static RoomNotAvailableException forDates(String roomId, String roomNumber, 
                                                    LocalDate checkIn, LocalDate checkOut) {
        String period = String.format("%s to %s", checkIn, checkOut);
        return new RoomNotAvailableException(
            String.format("Room %s is not available from %s to %s", roomNumber, checkIn, checkOut),
            roomId,
            roomNumber,
            period
        );
    }

    public static RoomNotAvailableException forDatesWithConflicts(String roomId, String roomNumber, 
                                                                LocalDate checkIn, LocalDate checkOut,
                                                                List<String> conflictingReservations) {
        String period = String.format("%s to %s", checkIn, checkOut);
        return new RoomNotAvailableException(
            String.format("Room %s is not available from %s to %s due to existing reservations", 
                         roomNumber, checkIn, checkOut),
            roomId,
            roomNumber,
            period,
            conflictingReservations,
            null
        );
    }

    public static RoomNotAvailableException forDatesWithAlternatives(String roomId, String roomNumber, 
                                                                   LocalDate checkIn, LocalDate checkOut,
                                                                   List<String> conflictingReservations,
                                                                   Map<String, Object> alternatives) {
        String period = String.format("%s to %s", checkIn, checkOut);
        return new RoomNotAvailableException(
            String.format("Room %s is not available from %s to %s. Alternative options available.", 
                         roomNumber, checkIn, checkOut),
            roomId,
            roomNumber,
            period,
            conflictingReservations,
            alternatives
        );
    }

    public static RoomNotAvailableException maintenanceBlock(String roomId, String roomNumber, 
                                                           LocalDate checkIn, LocalDate checkOut) {
        String period = String.format("%s to %s", checkIn, checkOut);
        return new RoomNotAvailableException(
            String.format("Room %s is blocked for maintenance from %s to %s", 
                         roomNumber, checkIn, checkOut),
            roomId,
            roomNumber,
            period
        );
    }

    public static RoomNotAvailableException outOfService(String roomId, String roomNumber) {
        return new RoomNotAvailableException(
            String.format("Room %s is currently out of service", roomNumber),
            roomId,
            roomNumber,
            "indefinite"
        );
    }

    public static RoomNotAvailableException alreadyOccupied(String roomId, String roomNumber, 
                                                          String currentReservation) {
        return new RoomNotAvailableException(
            String.format("Room %s is currently occupied by reservation %s", 
                         roomNumber, currentReservation),
            roomId,
            roomNumber,
            "current",
            List.of(currentReservation),
            null
        );
    }
}