package com.hotel.reservation.dto.request;

import com.hotel.reservation.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User registration request data")
public class RegisterRequestDto {

    @Schema(description = "Unique username", example = "seba4s", required = true)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers and underscores")
    private String username;

    @Schema(description = "User email address", example = "seba4s@example.com", required = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Schema(description = "User password", example = "SecurePassword123!", required = true)
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", 
             message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit")
    private String password;

    @Schema(description = "Password confirmation", example = "SecurePassword123!", required = true)
    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;

    @Schema(description = "User first name", example = "Sebastian", required = true)
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @Schema(description = "User last name", example = "García", required = true)
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @Schema(description = "Phone number", example = "+57 300 123 4567", required = false)
    @Pattern(regexp = "^[\\+]?[1-9][\\d\\s\\-\\(\\)]{8,15}$", 
             message = "Phone number format is invalid")
    private String phone;

    @Schema(description = "Country code", example = "CO", required = false)
    @Size(min = 2, max = 3, message = "Country code must be 2 or 3 characters")
    private String country;

    @Schema(description = "User role", example = "CLIENT", required = false)
    @Builder.Default
    private User.Role role = User.Role.CLIENT;

    /**
     * Custom validation method to check if passwords match
     */
    public boolean isPasswordConfirmed() {
        return password != null && password.equals(confirmPassword);
    }
}