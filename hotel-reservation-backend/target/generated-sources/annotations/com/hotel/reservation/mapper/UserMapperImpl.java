package com.hotel.reservation.mapper;

import com.hotel.reservation.dto.request.RegisterRequestDto;
import com.hotel.reservation.dto.response.ReservationResponseDto;
import com.hotel.reservation.dto.response.UserResponseDto;
import com.hotel.reservation.model.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-22T06:35:59+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.17 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponseDto toResponseDto(User user) {
        validateEntity( user );

        if ( user == null ) {
            return null;
        }

        UserResponseDto.UserResponseDtoBuilder userResponseDto = UserResponseDto.builder();

        userResponseDto.id( user.getId() );
        userResponseDto.username( user.getUsername() );
        userResponseDto.email( user.getEmail() );
        userResponseDto.firstName( user.getFirstName() );
        userResponseDto.lastName( user.getLastName() );
        userResponseDto.phone( user.getPhone() );
        userResponseDto.country( user.getCountry() );
        userResponseDto.role( user.getRole() );
        userResponseDto.enabled( user.getEnabled() );
        userResponseDto.accountNonLocked( user.getAccountNonLocked() );
        userResponseDto.createdAt( user.getCreatedAt() );
        userResponseDto.updatedAt( user.getUpdatedAt() );

        return userResponseDto.build();
    }

    @Override
    public User toEntity(RegisterRequestDto registerRequest) {
        if ( registerRequest == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.username( registerRequest.getUsername() );
        user.email( registerRequest.getEmail() );
        user.firstName( registerRequest.getFirstName() );
        user.lastName( registerRequest.getLastName() );
        user.phone( registerRequest.getPhone() );
        user.country( registerRequest.getCountry() );
        user.role( registerRequest.getRole() );

        user.enabled( true );
        user.accountNonLocked( true );

        return user.build();
    }

    @Override
    public void updateEntityFromDto(RegisterRequestDto dto, User entity) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getUsername() != null ) {
            entity.setUsername( dto.getUsername() );
        }
        if ( dto.getEmail() != null ) {
            entity.setEmail( dto.getEmail() );
        }
        if ( dto.getFirstName() != null ) {
            entity.setFirstName( dto.getFirstName() );
        }
        if ( dto.getLastName() != null ) {
            entity.setLastName( dto.getLastName() );
        }
        if ( dto.getPhone() != null ) {
            entity.setPhone( dto.getPhone() );
        }
        if ( dto.getCountry() != null ) {
            entity.setCountry( dto.getCountry() );
        }
    }

    @Override
    public ReservationResponseDto.GuestDto toGuestDto(User user) {
        validateEntity( user );

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
    public UserResponseDto toPublicResponseDto(User user) {
        validateEntity( user );

        if ( user == null ) {
            return null;
        }

        UserResponseDto.UserResponseDtoBuilder userResponseDto = UserResponseDto.builder();

        userResponseDto.id( user.getId() );
        userResponseDto.username( user.getUsername() );
        userResponseDto.email( user.getEmail() );
        userResponseDto.firstName( user.getFirstName() );
        userResponseDto.lastName( user.getLastName() );
        userResponseDto.country( user.getCountry() );
        userResponseDto.role( user.getRole() );
        userResponseDto.createdAt( user.getCreatedAt() );

        return userResponseDto.build();
    }
}
