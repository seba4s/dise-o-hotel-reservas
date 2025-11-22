package com.hotel.reservation.dto.response;

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
@Schema(description = "Login response with authentication tokens")
public class LoginResponseDto {

    @Schema(description = "JWT access token", 
            example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzZWJhNHMi...", required = true)
    private String accessToken;

    @Schema(description = "JWT refresh token for token renewal", 
            example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzZWJhNHMi...", required = true)
    private String refreshToken;

    @Schema(description = "Token type", example = "Bearer", required = true)
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Token expiration time in seconds", example = "86400", required = true)
    private Long expiresIn;

    @Schema(description = "Token expiration timestamp", 
            example = "2025-11-23T03:51:37", required = true)
    private LocalDateTime expiresAt;

    @Schema(description = "Authenticated user information", required = true)
    private UserResponseDto user;

    @Schema(description = "Login timestamp", 
            example = "2025-11-22T03:51:37", required = true)
    private LocalDateTime loginTime;

    @Schema(description = "User's IP address", example = "192.168.1.100", required = false)
    private String ipAddress;

    @Schema(description = "Session ID for tracking", 
            example = "sess_12345abcde", required = false)
    private String sessionId;

    @Schema(description = "First time login flag", example = "false", required = false)
    @Builder.Default
    private Boolean firstTimeLogin = false;

    @Schema(description = "Password change required flag", example = "false", required = false)
    @Builder.Default
    private Boolean passwordChangeRequired = false;
}