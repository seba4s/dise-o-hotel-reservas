package com.hotel.reservation.mapper;

import com.hotel.reservation.dto.response.CheckInResponseDto;
import com.hotel.reservation.dto.response.CheckOutResponseDto;
import com.hotel.reservation.dto.response.ReservationResponseDto;
import com.hotel.reservation.dto.response.RoomAvailabilityDto;
import com.hotel.reservation.model.Room;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-22T06:25:21+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.17 (Eclipse Adoptium)"
)
@Component
public class RoomMapperImpl implements RoomMapper {

    @Override
    public RoomAvailabilityDto toAvailabilityDto(Room room) {
        if ( room == null ) {
            return null;
        }

        RoomAvailabilityDto.RoomAvailabilityDtoBuilder roomAvailabilityDto = RoomAvailabilityDto.builder();

        roomAvailabilityDto.id( room.getId() );
        roomAvailabilityDto.roomNumber( room.getRoomNumber() );
        roomAvailabilityDto.roomType( room.getRoomType() );
        roomAvailabilityDto.capacity( room.getCapacity() );
        roomAvailabilityDto.size( room.getSize() );
        roomAvailabilityDto.bedType( room.getBedType() );
        roomAvailabilityDto.basePrice( room.getBasePrice() );
        roomAvailabilityDto.description( room.getDescription() );
        List<String> list = room.getAmenities();
        if ( list != null ) {
            roomAvailabilityDto.amenities( new ArrayList<String>( list ) );
        }
        List<String> list1 = room.getPhotos();
        if ( list1 != null ) {
            roomAvailabilityDto.photos( new ArrayList<String>( list1 ) );
        }
        roomAvailabilityDto.pricePerNight( room.getBasePrice() );

        roomAvailabilityDto.currency( "COP" );
        roomAvailabilityDto.available( true );
        roomAvailabilityDto.lastChecked( java.time.LocalDateTime.now() );
        roomAvailabilityDto.instantBooking( true );

        return roomAvailabilityDto.build();
    }

    @Override
    public RoomAvailabilityDto toAvailabilityDtoWithDates(Room room, LocalDate checkInDate, LocalDate checkOutDate, Boolean available) {
        if ( room == null && checkInDate == null && checkOutDate == null && available == null ) {
            return null;
        }

        RoomAvailabilityDto.RoomAvailabilityDtoBuilder roomAvailabilityDto = RoomAvailabilityDto.builder();

        if ( room != null ) {
            roomAvailabilityDto.id( room.getId() );
            roomAvailabilityDto.roomNumber( room.getRoomNumber() );
            roomAvailabilityDto.roomType( room.getRoomType() );
            roomAvailabilityDto.capacity( room.getCapacity() );
            roomAvailabilityDto.size( room.getSize() );
            roomAvailabilityDto.bedType( room.getBedType() );
            roomAvailabilityDto.basePrice( room.getBasePrice() );
            roomAvailabilityDto.description( room.getDescription() );
            List<String> list = room.getAmenities();
            if ( list != null ) {
                roomAvailabilityDto.amenities( new ArrayList<String>( list ) );
            }
            List<String> list1 = room.getPhotos();
            if ( list1 != null ) {
                roomAvailabilityDto.photos( new ArrayList<String>( list1 ) );
            }
            roomAvailabilityDto.pricePerNight( room.getBasePrice() );
        }
        roomAvailabilityDto.checkInDate( checkInDate );
        roomAvailabilityDto.checkOutDate( checkOutDate );
        roomAvailabilityDto.available( available );
        roomAvailabilityDto.currency( "COP" );
        roomAvailabilityDto.lastChecked( java.time.LocalDateTime.now() );
        roomAvailabilityDto.instantBooking( true );

        return roomAvailabilityDto.build();
    }

    @Override
    public ReservationResponseDto.RoomDto toReservationRoomDto(Room room) {
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

    @Override
    public CheckOutResponseDto.RoomDto toCheckOutRoomDto(Room room) {
        if ( room == null ) {
            return null;
        }

        CheckOutResponseDto.RoomDto.RoomDtoBuilder roomDto = CheckOutResponseDto.RoomDto.builder();

        roomDto.roomNumber( room.getRoomNumber() );
        roomDto.roomType( room.getRoomType() );

        return roomDto.build();
    }
}
