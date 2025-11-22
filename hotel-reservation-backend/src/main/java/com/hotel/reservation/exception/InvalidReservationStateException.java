package com.hotel.reservation.exception;

import com.hotel.reservation.model.Reservation;
import lombok.Getter;

import java.util.List;

/**
 * Exception thrown when attempting an operation on a reservation with invalid state
 * Used specifically for check-in/check-out operations in HU009 and HU010
 */
@Getter
public class InvalidReservationStateException extends RuntimeException {

    private final String reservationId;
    private final String confirmationNumber;
    private final Reservation.ReservationStatus currentState;
    private final Reservation.ReservationStatus requiredState;
    private final String operation;
    private final List<Reservation.ReservationStatus> validStates;

    public InvalidReservationStateException(String message) {
        super(message);
        this.reservationId = null;
        this.confirmationNumber = null;
        this.currentState = null;
        this.requiredState = null;
        this.operation = null;
        this.validStates = null;
    }

    public InvalidReservationStateException(String message, String reservationId, 
                                          Reservation.ReservationStatus currentState,
                                          Reservation.ReservationStatus requiredState) {
        super(message);
        this.reservationId = reservationId;
        this.confirmationNumber = null;
        this.currentState = currentState;
        this.requiredState = requiredState;
        this.operation = null;
        this.validStates = null;
    }

    public InvalidReservationStateException(String message, String reservationId, 
                                          String confirmationNumber,
                                          Reservation.ReservationStatus currentState,
                                          String operation,
                                          List<Reservation.ReservationStatus> validStates) {
        super(message);
        this.reservationId = reservationId;
        this.confirmationNumber = confirmationNumber;
        this.currentState = currentState;
        this.requiredState = null;
        this.operation = operation;
        this.validStates = validStates;
    }

    /**
     * Static factory methods for specific state transition errors
     */
    public static InvalidReservationStateException cannotCheckIn(String reservationId, 
                                                               String confirmationNumber,
                                                               Reservation.ReservationStatus currentState) {
        return new InvalidReservationStateException(
            String.format("Cannot check-in reservation %s in state %s. Valid states: CONFIRMED", 
                         confirmationNumber, currentState),
            reservationId,
            confirmationNumber,
            currentState,
            "CHECK_IN",
            List.of(Reservation.ReservationStatus.CONFIRMED)
        );
    }

    public static InvalidReservationStateException cannotCheckOut(String reservationId, 
                                                                String confirmationNumber,
                                                                Reservation.ReservationStatus currentState) {
        return new InvalidReservationStateException(
            String.format("Cannot check-out reservation %s in state %s. Valid states: CHECKED_IN", 
                         confirmationNumber, currentState),
            reservationId,
            confirmationNumber,
            currentState,
            "CHECK_OUT",
            List.of(Reservation.ReservationStatus.CHECKED_IN)
        );
    }

    public static InvalidReservationStateException cannotModify(String reservationId, 
                                                              String confirmationNumber,
                                                              Reservation.ReservationStatus currentState) {
        return new InvalidReservationStateException(
            String.format("Cannot modify reservation %s in state %s. Valid states: PRE_RESERVATION", 
                         confirmationNumber, currentState),
            reservationId,
            confirmationNumber,
            currentState,
            "MODIFY",
            List.of(Reservation.ReservationStatus.PRE_RESERVATION)
        );
    }

    public static InvalidReservationStateException cannotCancel(String reservationId, 
                                                              String confirmationNumber,
                                                              Reservation.ReservationStatus currentState) {
        List<Reservation.ReservationStatus> validStates = List.of(
            Reservation.ReservationStatus.PRE_RESERVATION,
            Reservation.ReservationStatus.CONFIRMED
        );
        
        return new InvalidReservationStateException(
            String.format("Cannot cancel reservation %s in state %s. Valid states: %s", 
                         confirmationNumber, currentState, validStates),
            reservationId,
            confirmationNumber,
            currentState,
            "CANCEL",
            validStates
        );
    }

    public static InvalidReservationStateException alreadyCheckedIn(String reservationId, 
                                                                  String confirmationNumber) {
        return new InvalidReservationStateException(
            String.format("Reservation %s is already checked in", confirmationNumber),
            reservationId,
            confirmationNumber,
            Reservation.ReservationStatus.CHECKED_IN,
            "CHECK_IN",
            List.of(Reservation.ReservationStatus.CONFIRMED)
        );
    }

    public static InvalidReservationStateException alreadyCheckedOut(String reservationId, 
                                                                   String confirmationNumber) {
        return new InvalidReservationStateException(
            String.format("Reservation %s is already checked out", confirmationNumber),
            reservationId,
            confirmationNumber,
            Reservation.ReservationStatus.CHECKED_OUT,
            "CHECK_OUT",
            List.of(Reservation.ReservationStatus.CHECKED_IN)
        );
    }

    public static InvalidReservationStateException reservationCancelled(String reservationId, 
                                                                      String confirmationNumber) {
        return new InvalidReservationStateException(
            String.format("Cannot operate on cancelled reservation %s", confirmationNumber),
            reservationId,
            confirmationNumber,
            Reservation.ReservationStatus.CANCELLED,
            "ANY_OPERATION",
            List.of(Reservation.ReservationStatus.CONFIRMED, 
                   Reservation.ReservationStatus.CHECKED_IN)
        );
    }

    public static InvalidReservationStateException noShow(String reservationId, 
                                                        String confirmationNumber) {
        return new InvalidReservationStateException(
            String.format("Reservation %s marked as no-show", confirmationNumber),
            reservationId,
            confirmationNumber,
            Reservation.ReservationStatus.NO_SHOW,
            "CHECK_IN",
            List.of(Reservation.ReservationStatus.CONFIRMED)
        );
    }
}