package com.hotel.reservation.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "users")
public class User implements UserDetails {
    
    @Id
    private String id;
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Indexed(unique = true)
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Indexed(unique = true)
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;
    
    private String phone;
    private String country;
    private String nationalId;
    
    @Builder.Default
    private Role role = Role.CLIENT;
    
    @Builder.Default
    private Boolean enabled = true;
    
    @Builder.Default
    private Boolean accountNonLocked = true;
    
    // Profile settings
    private UserPreferences preferences;
    
    // Authentication tracking
    private LocalDateTime lastLogin;
    private String lastLoginIp;
    private Integer failedLoginAttempts;
    private LocalDateTime lockedUntil;
    
    // Email verification
    private Boolean emailVerified;
    private String emailVerificationToken;
    private LocalDateTime emailVerificationExpiry;
    
    // Password reset
    private String passwordResetToken;
    private LocalDateTime passwordResetExpiry;
    
    // 2FA settings
    private Boolean twoFactorEnabled;
    private String twoFactorSecret;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked && (lockedUntil == null || LocalDateTime.now().isAfter(lockedUntil));
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * User roles in the system
     */
    public enum Role {
        CLIENT("Guest/Customer role"),
        STAFF("Hotel staff role"),
        ADMIN("Administrator role"),
        AUDITOR("Auditor role for logs");
        
        private final String description;
        
        Role(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * User preferences and settings
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserPreferences {
        // Notification preferences
        @Builder.Default
        private Boolean emailNotifications = true;
        
        @Builder.Default
        private Boolean smsNotifications = false;
        
        @Builder.Default
        private Boolean pushNotifications = true;
        
        // Communication preferences
        @Builder.Default
        private String preferredLanguage = "es";

        @Builder.Default
        private String timezone = "America/Bogota";

        @Builder.Default
        private String currency = "COP";
        
        // Marketing preferences
        @Builder.Default
        private Boolean marketingEmails = false;
        
        @Builder.Default
        private Boolean promotionalOffers = true;
        
        // Privacy settings
        @Builder.Default
        private Boolean profilePublic = false;
        
        @Builder.Default
        private Boolean shareDataForImprovement = true;
    }
    
    /**
     * Business methods
     */
    
    public String getFullName() {
        return String.format("%s %s", firstName, lastName);
    }
    
    public boolean isAdmin() {
        return Role.ADMIN.equals(role);
    }
    
    public boolean isStaff() {
        return Role.STAFF.equals(role) || isAdmin();
    }
    
    public boolean isClient() {
        return Role.CLIENT.equals(role);
    }
    
    public boolean isAuditor() {
        return Role.AUDITOR.equals(role) || isAdmin();
    }
    
    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts = (this.failedLoginAttempts != null ? this.failedLoginAttempts : 0) + 1;
        
        // Lock account after 5 failed attempts for 30 minutes
        if (this.failedLoginAttempts >= 5) {
            this.accountNonLocked = false;
            this.lockedUntil = LocalDateTime.now().plusMinutes(30);
        }
    }
    
    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.accountNonLocked = true;
        this.lockedUntil = null;
    }
    
    public void updateLastLogin(String ipAddress) {
        this.lastLogin = LocalDateTime.now();
        this.lastLoginIp = ipAddress;
        resetFailedLoginAttempts();
    }
    
    public boolean isEmailVerificationExpired() {
        return emailVerificationExpiry != null && LocalDateTime.now().isAfter(emailVerificationExpiry);
    }
    
    public boolean isPasswordResetTokenExpired() {
        return passwordResetExpiry != null && LocalDateTime.now().isAfter(passwordResetExpiry);
    }
    
    public void generateEmailVerificationToken() {
        this.emailVerificationToken = java.util.UUID.randomUUID().toString();
        this.emailVerificationExpiry = LocalDateTime.now().plusDays(1); // 24 hours
    }
    
    public void generatePasswordResetToken() {
        this.passwordResetToken = java.util.UUID.randomUUID().toString();
        this.passwordResetExpiry = LocalDateTime.now().plusHours(2); // 2 hours
    }
    
    public void verifyEmail() {
        this.emailVerified = true;
        this.emailVerificationToken = null;
        this.emailVerificationExpiry = null;
    }
}