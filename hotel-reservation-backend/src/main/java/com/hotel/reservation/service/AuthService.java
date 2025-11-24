package com.hotel.reservation.service;

import com.hotel.reservation.dto.request.LoginRequestDto;
import com.hotel.reservation.dto.request.RegisterRequestDto;
import com.hotel.reservation.dto.response.LoginResponseDto;
import com.hotel.reservation.dto.response.UserResponseDto;
import com.hotel.reservation.exception.BadRequestException;
import com.hotel.reservation.exception.ConflictException;
import com.hotel.reservation.exception.UnauthorizedException;
import com.hotel.reservation.mapper.UserMapper;
import com.hotel.reservation.model.User;
import com.hotel.reservation.repository.UserRepository;
import com.hotel.reservation.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final UserDetailsService userDetailsService;

    /**
     * User login with JWT token generation
     */
    @Transactional
    public LoginResponseDto login(LoginRequestDto loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getUsername());
        
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new UnauthorizedException("User not found"));

            // Check if user is enabled and not locked
            if (!user.isEnabled()) {
                throw new UnauthorizedException("Account is disabled");
            }
            
            if (!user.isAccountNonLocked()) {
                throw new UnauthorizedException("Account is locked");
            }

            // Generate tokens
            String accessToken = jwtUtil.generateToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user);
            
            // Update last login info
            user.updateLastLogin(getClientIpAddress());
            userRepository.save(user);

            // Build response
            UserResponseDto userResponse = userMapper.toResponseDto(user);
            
            return LoginResponseDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getExpirationTime())
                    .expiresAt(LocalDateTime.now().plusSeconds(jwtUtil.getExpirationTime()))
                    .user(userResponse)
                    .loginTime(LocalDateTime.now())
                    .ipAddress(getClientIpAddress())
                    .sessionId(generateSessionId())
                    .firstTimeLogin(user.getLastLogin() == null)
                    .passwordChangeRequired(false)
                    .build();
                    
        } catch (BadCredentialsException e) {
            // Handle failed login attempts
            handleFailedLogin(loginRequest.getUsername());
            throw new UnauthorizedException("Invalid username or password");
        } catch (DisabledException e) {
            throw new UnauthorizedException("Account is disabled");
        }
    }

    /**
     * User registration
     */
    @Transactional
    public UserResponseDto register(RegisterRequestDto registerRequest) {
        log.info("Registration attempt for user: {} with email: {}", 
                registerRequest.getUsername(), registerRequest.getEmail());

        // Validate passwords match
        if (!registerRequest.isPasswordConfirmed()) {
            throw BadRequestException.passwordMismatch();
        }

        // Check if user already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw ConflictException.usernameAlreadyExists(registerRequest.getUsername());
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw ConflictException.emailAlreadyExists(registerRequest.getEmail());
        }

        // Create new user
        User user = userMapper.toEntity(registerRequest);
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmailVerified(false); // Email verification required
        user.generateEmailVerificationToken();

        // Save user
        user = userRepository.save(user);

        log.info("User registered successfully: {}", user.getUsername());
        
        // TODO: Send email verification email
        // emailService.sendEmailVerification(user);

        return userMapper.toResponseDto(user);
    }

    /**
     * Refresh JWT token
     */
    public LoginResponseDto refreshToken(String refreshToken) {
        log.info("Token refresh attempt");

        try {
            // Validate refresh token
            if (!jwtUtil.validateRefreshToken(refreshToken)) {
                throw new UnauthorizedException("Invalid refresh token");
            }

            String username = jwtUtil.getUsernameFromRefreshToken(refreshToken);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UnauthorizedException("User not found"));

            // Check if user is still active
            if (!user.isEnabled() || !user.isAccountNonLocked()) {
                throw new UnauthorizedException("Account is no longer active");
            }

            // Generate new tokens
            String newAccessToken = jwtUtil.generateToken(user);
            String newRefreshToken = jwtUtil.generateRefreshToken(user);

            UserResponseDto userResponse = userMapper.toResponseDto(user);

            return LoginResponseDto.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getExpirationTime())
                    .expiresAt(LocalDateTime.now().plusSeconds(jwtUtil.getExpirationTime()))
                    .user(userResponse)
                    .loginTime(LocalDateTime.now())
                    .ipAddress(getClientIpAddress())
                    .sessionId(generateSessionId())
                    .firstTimeLogin(false)
                    .passwordChangeRequired(false)
                    .build();

        } catch (Exception e) {
            throw new UnauthorizedException("Token refresh failed: " + e.getMessage());
        }
    }

    /**
     * User logout (token invalidation could be implemented with a blacklist)
     */
    public void logout(String token) {
        log.info("Logout attempt");
        
        try {
            String username = jwtUtil.getUsernameFromToken(token);
            log.info("User {} logged out successfully", username);
            
            // TODO: Implement token blacklist if needed
            // tokenBlacklistService.addToBlacklist(token);
            
            // Clear security context
            SecurityContextHolder.clearContext();
            
        } catch (Exception e) {
            log.warn("Error during logout: {}", e.getMessage());
        }
    }

    /**
     * Handle failed login attempts
     */
    private void handleFailedLogin(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.incrementFailedLoginAttempts();
            userRepository.save(user);
            
            log.warn("Failed login attempt #{} for user: {}", 
                    user.getFailedLoginAttempts(), username);
        });
    }

    /**
     * Get client IP address (simplified - in real implementation would get from HttpServletRequest)
     */
    private String getClientIpAddress() {
        return "192.168.1.100"; // Placeholder
    }

    /**
     * Generate session ID
     */
    private String generateSessionId() {
        return "sess_" + System.currentTimeMillis() + "_" + 
               java.util.UUID.randomUUID().toString().substring(0, 8);
    }
}