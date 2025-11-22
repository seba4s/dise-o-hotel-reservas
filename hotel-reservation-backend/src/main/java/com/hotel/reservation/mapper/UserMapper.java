package com.hotel.reservation.mapper;

import com.hotel.reservation.dto.request.RegisterRequestDto;
import com.hotel.reservation.dto.response.UserResponseDto;
import com.hotel.reservation.model.User;
import org.mapstruct.*;

import java.time.LocalDateTime;

/**
 * MapStruct mapper for User entity and DTOs
 * Handles conversions between User model and request/response DTOs
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {DateTimeMapper.class}
)
public interface UserMapper {

    /**
     * Convert User entity to UserResponseDto
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "country", source = "country")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "enabled", source = "enabled")
    @Mapping(target = "accountNonLocked", source = "accountNonLocked")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "lastLogin", ignore = true) // Set separately if needed
    UserResponseDto toResponseDto(User user);

    /**
     * Convert RegisterRequestDto to User entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "password", ignore = true) // Password will be encoded separately
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "country", source = "country")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "accountNonLocked", constant = "true")
    @Mapping(target = "createdAt", ignore = true) // Set by @CreatedDate
    @Mapping(target = "updatedAt", ignore = true) // Set by @LastModifiedDate
    User toEntity(RegisterRequestDto registerRequest);

    /**
     * Update existing User entity with RegisterRequestDto data
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // Don't update password in profile update
    @Mapping(target = "role", ignore = true) // Role changes handled separately
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "accountNonLocked", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(RegisterRequestDto dto, @MappingTarget User entity);

    /**
     * Create a Guest DTO for reservations from User entity
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "country", source = "country")
    com.hotel.reservation.dto.response.ReservationResponseDto.GuestDto toGuestDto(User user);

    /**
     * After mapping method to set additional fields
     */
    @AfterMapping
    default void setAdditionalFields(@MappingTarget UserResponseDto dto, User entity) {
        // Set last login if available (would come from session management)
        dto.setLastLogin(LocalDateTime.now().minusDays(1)); // Example
    }

    /**
     * Before mapping method for validation
     */
    @BeforeMapping
    default void validateEntity(User entity) {
        if (entity == null) {
            throw new IllegalArgumentException("User entity cannot be null");
        }
    }

    /**
     * Custom mapping for sensitive data filtering
     */
    @Named("publicUserInfo")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "phone", ignore = true) // Hide phone in public view
    @Mapping(target = "country", source = "country")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "accountNonLocked", ignore = true)
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    UserResponseDto toPublicResponseDto(User user);
}