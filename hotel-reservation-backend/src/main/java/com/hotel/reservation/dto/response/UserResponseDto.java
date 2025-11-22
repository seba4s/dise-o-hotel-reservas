package com.hotel.reservation.dto.response;

import com.hotel.reservation.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User information response")
public class UserResponseDto {

    @Schema(description = "User unique identifier", example = "64abc123def456789012", required = true)
    private String id;

    @Schema(description = "Username", example = "seba4s", required = true)
    private String username;

    @Schema(description = "User email", example = "seba4s@example.com", required = true)
    private String email;

    @Schema(description = "User first name", example = "Sebastian", required = true)
    private String firstName;

    @Schema(description = "User last name", example = "García", required = true)
    private String lastName;

    @Schema(description = "Phone number", example = "+57 300 123 4567", required = false)
    private String phone;

    @Schema(description = "Country code", example = "CO", required = false)
    private String country;

    @Schema(description = "User role in the system", example = "CLIENT", required = true)
    private User.Role role;

    @Schema(description = "Account enabled status", example = "true", required = true)
    private Boolean enabled;

    @Schema(description = "Account locked status", example = "false", required = true)
    private Boolean accountNonLocked;

    @Schema(description = "User registration date", example = "2025-11-01T10:30:00", required = true)
    private LocalDateTime createdAt;

    @Schema(description = "Last profile update", example = "2025-11-22T03:51:37", required = true)
    private LocalDateTime updatedAt;

    @Schema(description = "Last login timestamp", example = "2025-11-22T03:45:20", required = false)
    private LocalDateTime lastLogin;

    /**
     * Get user's full name
     */
    public String getFullName() {
        return String.format("%s %s", firstName, lastName);
    }

    /**
     * Check if user is admin
     */
    public Boolean isAdmin() {
        return User.Role.ADMIN.equals(role);
    }

    /**
     * Check if user is staff
     */
    public Boolean isStaff() {
        return User.Role.STAFF.equals(role) || isAdmin();
    }

    /**
     * Check if user is client
     */
    public Boolean isClient() {
        return User.Role.CLIENT.equals(role);
    }
}