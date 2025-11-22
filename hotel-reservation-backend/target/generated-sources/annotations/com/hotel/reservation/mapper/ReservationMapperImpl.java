package com.hotel.reservation.mapper;

import com.hotel.reservation.dto.request.CreateReservationDto;
import com.hotel.reservation.dto.response.ReservationResponseDto;
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
    date = "2025-11-22T06:25:21+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.17 (Eclipse Adoptium)"
)
@Component
public class ReservationMapperImpl implements ReservationMapper {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ReservationResponseDto toResponseDto(Reservation reservation) {
        validateReservation( reservation );

        if ( reservation == null ) {
            return null;
        }

        ReservationResponseDto.ReservationResponseDtoBuilder reservationResponseDto = ReservationResponseDto.builder();

        reservationResponseDto.id( reservation.getId() );
        reservationResponseDto.confirmationNumber( reservation.getConfirmationNumber() );
        reservationResponseDto.guest( toGuestDto( reservation.getGuest() ) );
        reservationResponseDto.room( toRoomDto( reservation.getRoom() ) );
        reservationResponseDto.checkInDate( reservation.getCheckInDate() );
        reservationResponseDto.checkOutDate( reservation.getCheckOutDate() );
        reservationResponseDto.adults( reservation.getAdults() );
        reservationResponseDto.children( reservation.getChildren() );
        reservationResponseDto.totalAmount( reservation.getTotalAmount() );
        reservationResponseDto.status( reservation.getStatus() );
        reservationResponseDto.specialRequests( reservation.getSpecialRequests() );
        reservationResponseDto.actualCheckInTime( reservation.getActualCheckInTime() );
        reservationResponseDto.actualCheckOutTime( reservation.getActualCheckOutTime() );
        reservationResponseDto.checkInStaff( reservation.getCheckInStaff() );
        reservationResponseDto.checkOutStaff( reservation.getCheckOutStaff() );
        reservationResponseDto.paymentMethod( reservation.getPaymentMethod() );
        reservationResponseDto.paymentStatus( reservation.getPaymentStatus() );
        reservationResponseDto.paymentTransactionId( reservation.getPaymentTransactionId() );
        reservationResponseDto.createdAt( reservation.getCreatedAt() );
        reservationResponseDto.updatedAt( reservation.getUpdatedAt() );

        reservationResponseDto.currency( "COP" );

        return reservationResponseDto.build();
    }

    @Override
    public Reservation toEntity(CreateReservationDto dto) {
        if ( dto == null ) {
            return null;
        }

        Reservation.ReservationBuilder reservation = Reservation.builder();

        reservation.checkInDate( dto.getCheckInDate() );
        reservation.checkOutDate( dto.getCheckOutDate() );
        reservation.adults( dto.getAdults() );
        reservation.children( dto.getChildren() );
        reservation.specialRequests( dto.getSpecialRequests() );
        reservation.paymentMethod( dto.getPaymentMethod() );
        reservation.promoCode( dto.getPromoCode() );

        reservation.status( Reservation.ReservationStatus.PRE_RESERVATION );

        return reservation.build();
    }

    @Override
    public void updateEntityFromDto(CreateReservationDto dto, Reservation entity) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getCheckInDate() != null ) {
            entity.setCheckInDate( dto.getCheckInDate() );
        }
        if ( dto.getCheckOutDate() != null ) {
            entity.setCheckOutDate( dto.getCheckOutDate() );
        }
        if ( dto.getAdults() != null ) {
            entity.setAdults( dto.getAdults() );
        }
        if ( dto.getChildren() != null ) {
            entity.setChildren( dto.getChildren() );
        }
        if ( dto.getSpecialRequests() != null ) {
            entity.setSpecialRequests( dto.getSpecialRequests() );
        }
        if ( dto.getPaymentMethod() != null ) {
            entity.setPaymentMethod( dto.getPaymentMethod() );
        }
        if ( dto.getPromoCode() != null ) {
            entity.setPromoCode( dto.getPromoCode() );
        }
    }

    @Override
    public ReservationResponseDto.GuestDto toGuestDto(User user) {
        userMapper.validateEntity( user );

        if ( user == null ) {
            return null;
        }

        ReservationResponseDto.GuestDto.GuestDtoBuilder guestDto = ReservationResponseDto.GuestDto.builder();

        guestDto.id( user.getId() );
        guestDto.email( user.getEmail() );
        guestDto.firstName( user.getFirstName() );
        guestDto.lastName( user.getLastName() );
        guestDto.phone( user.getPhone() );
        guestDto.country( user.getCountry() );

        return guestDto.build();
    }

    @Override
    public ReservationResponseDto.RoomDto toRoomDto(Room room) {
        if ( room == null ) {
            return null;
        }

        ReservationResponseDto.RoomDto.RoomDtoBuilder roomDto = ReservationResponseDto.RoomDto.builder();

        roomDto.id( room.getId() );
        roomDto.roomNumber( room.getRoomNumber() );
        roomDto.roomType( room.getRoomType() );
        roomDto.capacity( room.getCapacity() );
        roomDto.bedType( room.getBedType() );
        roomDto.basePrice( room.getBasePrice() );
        List<String> list = room.getAmenities();
        if ( list != null ) {
            roomDto.amenities( new ArrayList<String>( list ) );
        }

        return roomDto.build();
    }
}
