package com.hotel.reservation.util;

import com.hotel.reservation.config.JwtConfig;
import com.hotel.reservation.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtConfig jwtConfig;

    /**
     * Get secret key for JWT signing
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes());
    }

    /**
     * Generate JWT token for user
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole().name());
        claims.put("fullName", user.getFullName());
        claims.put("enabled", user.getEnabled());
        claims.put("accountNonLocked", user.getAccountNonLocked());
        
        return createToken(claims, user.getUsername(), jwtConfig.getExpiration());
    }

    /**
     * Generate refresh token
     */
    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("type", "refresh");
        
        return createToken(claims, user.getUsername(), jwtConfig.getRefreshExpiration());
    }

    /**
     * Create token with claims
     */
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuer(jwtConfig.getIssuer())
                .setAudience(jwtConfig.getAudience())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Extract username from token
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Extract user ID from token
     */
    public String getUserIdFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("userId", String.class));
    }

    /**
     * Extract user role from token
     */
    public String getUserRoleFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("role", String.class));
    }

    /**
     * Extract expiration date from token
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Extract specific claim from token
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from token
     */
    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .requireIssuer(jwtConfig.getIssuer())
                    .requireAudience(jwtConfig.getAudience())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            throw new RuntimeException("JWT token is expired", e);
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
            throw new RuntimeException("JWT token is unsupported", e);
        } catch (MalformedJwtException e) {
            log.error("JWT token is malformed: {}", e.getMessage());
            throw new RuntimeException("JWT token is malformed", e);
        } catch (SecurityException e) {
            log.error("JWT signature validation failed: {}", e.getMessage());
            throw new RuntimeException("JWT signature validation failed", e);
        } catch (IllegalArgumentException e) {
            log.error("JWT token compact of handler are invalid: {}", e.getMessage());
            throw new RuntimeException("JWT token compact of handler are invalid", e);
        }
    }

    /**
     * Check if token is expired
     */
    public Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            log.warn("Error checking token expiration: {}", e.getMessage());
            return true; // Consider expired if we can't parse
        }
    }

    /**
     * Validate token against user details
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = getUsernameFromToken(token);
            final Boolean isExpired = isTokenExpired(token);
            
            boolean isValid = username.equals(userDetails.getUsername()) && 
                             !isExpired && 
                             userDetails.isEnabled() && 
                             userDetails.isAccountNonLocked();
            
            if (isValid) {
                log.debug("Token validation successful for user: {}", username);
            } else {
                log.warn("Token validation failed for user: {} - expired: {}, enabled: {}, non-locked: {}", 
                        username, isExpired, userDetails.isEnabled(), userDetails.isAccountNonLocked());
            }
            
            return isValid;
            
        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate refresh token
     */
    public Boolean validateRefreshToken(String refreshToken) {
        try {
            Claims claims = getAllClaimsFromToken(refreshToken);
            String tokenType = claims.get("type", String.class);
            
            boolean isValid = "refresh".equals(tokenType) && !isTokenExpired(refreshToken);
            
            if (isValid) {
                log.debug("Refresh token validation successful");
            } else {
                log.warn("Refresh token validation failed - type: {}, expired: {}", 
                        tokenType, isTokenExpired(refreshToken));
            }
            
            return isValid;
            
        } catch (Exception e) {
            log.error("Refresh token validation error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract username from refresh token
     */
    public String getUsernameFromRefreshToken(String refreshToken) {
        return getUsernameFromToken(refreshToken);
    }

    /**
     * Get token expiration time in seconds
     */
    public Long getExpirationTime() {
        return jwtConfig.getExpiration() / 1000; // Convert to seconds
    }

    /**
     * Get refresh token expiration time in seconds
     */
    public Long getRefreshExpirationTime() {
        return jwtConfig.getRefreshExpiration() / 1000; // Convert to seconds
    }

    /**
     * Extract token creation date
     */
    public Date getIssuedAtDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getIssuedAt);
    }

    /**
     * Check if token can be refreshed
     */
    public Boolean canTokenBeRefreshed(String token) {
        try {
            return !isTokenExpired(token) || ignoreTokenExpiration(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Ignore token expiration for refresh (grace period)
     */
    private Boolean ignoreTokenExpiration(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            Date now = new Date();
            
            // Allow refresh within 5 minutes of expiration
            long gracePeriodMs = 5 * 60 * 1000; // 5 minutes in milliseconds
            return now.getTime() - expiration.getTime() <= gracePeriodMs;
            
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extract all user info from token
     */
    public Map<String, Object> getUserInfoFromToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            Map<String, Object> userInfo = new HashMap<>();
            
            userInfo.put("username", claims.getSubject());
            userInfo.put("userId", claims.get("userId"));
            userInfo.put("email", claims.get("email"));
            userInfo.put("role", claims.get("role"));
            userInfo.put("fullName", claims.get("fullName"));
            userInfo.put("enabled", claims.get("enabled"));
            userInfo.put("accountNonLocked", claims.get("accountNonLocked"));
            userInfo.put("issuedAt", claims.getIssuedAt());
            userInfo.put("expiresAt", claims.getExpiration());
            
            return userInfo;
            
        } catch (Exception e) {
            log.error("Error extracting user info from token: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Get time until token expires (in minutes)
     */
    public Long getMinutesUntilExpiration(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            Date now = new Date();
            
            if (expiration.before(now)) {
                return 0L; // Already expired
            }
            
            return (expiration.getTime() - now.getTime()) / (60 * 1000); // Convert to minutes
            
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Check if token is about to expire (within threshold)
     */
    public Boolean isTokenAboutToExpire(String token, Integer thresholdMinutes) {
        try {
            Long minutesUntilExpiration = getMinutesUntilExpiration(token);
            return minutesUntilExpiration <= thresholdMinutes;
        } catch (Exception e) {
            return true; // Consider about to expire if we can't parse
        }
    }

    /**
     * Create session token for current user context
     */
    public String createSessionToken() {
        return "sess_" + System.currentTimeMillis() + "_" + 
               java.util.UUID.randomUUID().toString().substring(0, 8);
    }
}