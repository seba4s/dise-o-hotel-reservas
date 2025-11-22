package com.hotel.reservation.mapper;

import com.hotel.reservation.dto.response.CheckInResponseDto;
import com.hotel.reservation.dto.response.CheckOutResponseDto;
import com.hotel.reservation.model.Reservation;
import com.hotel.reservation.model.Room;
import com.hotel.reservation.model.User;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-22T06:33:31+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.17 (Eclipse Adoptium)"
)
@Component
public class CheckInCheckOutMapperImpl implements CheckInCheckOutMapper {

    @Autowired
    private ReservationMapper reservationMapper;

    @Override
    public CheckOutResponseDto toCheckOutResponseDto(Reservation reservation) {
        reservationMapper.validateReservation( reservation );

        if ( reservation == null ) {
            return null;
        }

        CheckOutResponseDto.CheckOutResponseDtoBuilder checkOutResponseDto = CheckOutResponseDto.builder();

        checkOutResponseDto.reservationId( reservation.getId() );
        checkOutResponseDto.confirmationNumber( reservation.getConfirmationNumber() );
        checkOutResponseDto.guest( toCheckOutGuestDto( reservation.getGuest() ) );
        checkOutResponseDto.room( toCheckOutRoomDto( reservation.getRoom() ) );
        checkOutResponseDto.checkOutTime( reservation.getActualCheckOutTime() );
        checkOutResponseDto.processedBy( reservation.getCheckOutStaff() );
        checkOutResponseDto.finalAmount( reservation.getTotalAmount() );

        checkOutResponseDto.currency( "COP" );
        checkOutResponseDto.status( "COMPLETED" );

        return checkOutResponseDto.build();
    }

    @Override
    public CheckInResponseDto toCheckInResponseDto(Reservation reservation) {
        reservationMapper.validateReservation( reservation );

        if ( reservation == null ) {
            return null;
        }

        CheckInResponseDto.CheckInResponseDtoBuilder checkInResponseDto = CheckInResponseDto.builder();

        checkInResponseDto.reservationId( reservation.getId() );
        checkInResponseDto.confirmationNumber( reservation.getConfirmationNumber() );
        checkInResponseDto.guest( toCheckInGuestDto( reservation.getGuest() ) );
        checkInResponseDto.room( toCheckInRoomDto( reservation.getRoom() ) );
        checkInResponseDto.checkInTime( reservation.getActualCheckInTime() );
        checkInResponseDto.expectedCheckOutDate( reservation.getCheckOutDate() );
        checkInResponseDto.processedBy( reservation.getCheckInStaff() );

        checkInResponseDto.status( "COMPLETED" );

        return checkInResponseDto.build();
    }

    @Override
    public CheckOutResponseDto.GuestDto toCheckOutGuestDto(User user) {
        if ( user == null ) {
            return null;
        }

        CheckOutResponseDto.GuestDto.GuestDtoBuilder guestDto = CheckOutResponseDto.GuestDto.builder();

        guestDto.email( user.getEmail() );

        guestDto.fullName( user.getFirstName() + " " + user.getLastName() );

        return guestDto.build();
    }

    @Override
    public CheckOutResponseDto.RoomDto toCheckOutRoomDto(Room room) {
        if ( room == null ) {
            return null;
        }

        CheckOutResponseDto.RoomDto.RoomDtoBuilder roomDto = CheckOutResponseDto.RoomDto.builder();

        roomDto.roomNumber( room.getRoomNumber() );
        roomDto.roomType( room.getRoomType() );

        roomDto.currentStatus( "NEEDS_CLEANING" );
        roomDto.housekeepingPriority( "STANDARD" );

        return roomDto.build();
    }

    @Override
    public CheckInResponseDto.GuestDto toCheckInGuestDto(User user) {
        if ( user == null ) {
            return null;
        }

        CheckInResponseDto.GuestDto.GuestDtoBuilder guestDto = CheckInResponseDto.GuestDto.builder();

        guestDto.email( user.getEmail() );
        guestDto.phone( user.getPhone() );

        guestDto.fullName( user.getFirstName() + " " + user.getLastName() );
        guestDto.vipGuest( false );

        return guestDto.build();
    }

    @Override
    public CheckInResponseDto.RoomAssignmentDto toCheckInRoomDto(Room room) {
        if ( room == null ) {
            return null;
        }

        CheckInResponseDto.RoomAssignmentDto.RoomAssignmentDtoBuilder roomAssignmentDto = CheckInResponseDto.RoomAssignmentDto.builder();

        roomAssignmentDto.roomNumber( room.getRoomNumber() );
        roomAssignmentDto.roomType( room.getRoomType() );
        List<String> list = room.getAmenities();
        if ( list != null ) {
            roomAssignmentDto.features( new ArrayList<String>( list ) );
        }
        roomAssignmentDto.roomRate( room.getBasePrice() );

        return roomAssignmentDto.build();
    }
}
