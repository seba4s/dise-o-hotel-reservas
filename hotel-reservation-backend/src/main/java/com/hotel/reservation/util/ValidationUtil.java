package com.hotel.reservation.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ValidationUtil {

    // Email validation pattern (RFC 5322 compliant)
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    // Phone validation pattern (international format)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[\\+]?[1-9][\\d\\s\\-\\(\\)]{8,15}$"
    );
    
    // Username validation pattern
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_]{3,30}$"
    );
    
    // Room number validation pattern
    private static final Pattern ROOM_NUMBER_PATTERN = Pattern.compile(
        "^[0-9]{3,4}[A-Z]?$"
    );
    
    // Document number pattern
    private static final Pattern DOCUMENT_PATTERN = Pattern.compile(
        "^[A-Za-z0-9]{5,20}$"
    );

    /**
     * Validate email address
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        String trimmedEmail = email.trim();
        return EMAIL_PATTERN.matcher(trimmedEmail).matches() && 
               trimmedEmail.length() <= 100;
    }

    /**
     * Validate phone number
     */
    public static boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false; // Phone is optional, but if provided must be valid
        }
        
        String trimmedPhone = phone.trim();
        return PHONE_PATTERN.matcher(trimmedPhone).matches();
    }

    /**
     * Validate username
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        String trimmedUsername = username.trim();
        return USERNAME_PATTERN.matcher(trimmedUsername).matches() &&
               !isReservedUsername(trimmedUsername);
    }

    /**
     * Validate room number format
     */
    public static boolean isValidRoomNumber(String roomNumber) {
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            return false;
        }
        
        return ROOM_NUMBER_PATTERN.matcher(roomNumber.trim()).matches();
    }

    /**
     * Validate document number
     */
    public static boolean isValidDocumentNumber(String documentNumber) {
        if (documentNumber == null || documentNumber.trim().isEmpty()) {
            return false;
        }
        
        return DOCUMENT_PATTERN.matcher(documentNumber.trim()).matches();
    }

    /**
     * Validate name (first name or last name)
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        String trimmedName = name.trim();
        return trimmedName.length() >= 2 && 
               trimmedName.length() <= 50 &&
               trimmedName.matches("^[a-zA-ZÀ-ÿ\\s'\\-]+$"); // Letters, spaces, apostrophes, hyphens
    }

    /**
     * Validate guest capacity
     */
    public static boolean isValidGuestCapacity(Integer adults, Integer children) {
        if (adults == null || adults < 1) {
            return false;
        }
        
        if (children == null) {
            children = 0;
        }
        
        if (children < 0) {
            return false;
        }
        
        int totalGuests = adults + children;
        return totalGuests >= 1 && totalGuests <= 10;
    }

    /**
     * Validate reservation dates
     */
    public static boolean isValidReservationDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            return false;
        }
        
        LocalDate today = DateUtil.getCurrentDate();
        
        // Check-in must be today or in the future
        if (checkIn.isBefore(today)) {
            return false;
        }
        
        // Check-out must be after check-in
        if (checkOut.isBefore(checkIn) || checkOut.equals(checkIn)) {
            return false;
        }
        
        // Maximum stay duration (1 year)
        if (DateUtil.calculateNights(checkIn, checkOut) > 365) {
            return false;
        }
        
        return true;
    }

    /**
     * Validate monetary amount
     */
    public static boolean isValidMonetaryAmount(java.math.BigDecimal amount) {
        if (amount == null) {
            return false;
        }
        
        return amount.compareTo(java.math.BigDecimal.ZERO) >= 0 &&
               amount.compareTo(java.math.BigDecimal.valueOf(99999999)) <= 0; // Max 99,999,999
    }

    /**
     * Validate price range
     */
    public static boolean isValidPriceRange(String priceRange) {
        if (priceRange == null || priceRange.trim().isEmpty()) {
            return true; // Optional field
        }
        
        List<String> validRanges = List.of("LOW", "MEDIUM", "HIGH", "LUXURY");
        return validRanges.contains(priceRange.trim().toUpperCase());
    }

    /**
     * Validate room type
     */
    public static boolean isValidRoomType(String roomType) {
        if (roomType == null || roomType.trim().isEmpty()) {
            return true; // Optional field
        }
        
        String trimmed = roomType.trim();
        return trimmed.length() >= 3 && 
               trimmed.length() <= 50 &&
               trimmed.matches("^[a-zA-Z0-9\\s]+$");
    }

    /**
     * Validate document type
     */
    public static boolean isValidDocumentType(String documentType) {
        if (documentType == null || documentType.trim().isEmpty()) {
            return false;
        }
        
        List<String> validTypes = List.of("PASSPORT", "NATIONAL_ID", "DRIVER_LICENSE", "OTHER");
        return validTypes.contains(documentType.trim().toUpperCase());
    }

    /**
     * Validate country code (ISO 3166-1 alpha-2 or alpha-3)
     */
    public static boolean isValidCountryCode(String countryCode) {
        if (countryCode == null || countryCode.trim().isEmpty()) {
            return true; // Optional field
        }
        
        String trimmed = countryCode.trim().toUpperCase();
        return (trimmed.length() == 2 || trimmed.length() == 3) &&
               trimmed.matches("^[A-Z]+$");
    }

    /**
     * Validate rating (1-5 scale)
     */
    public static boolean isValidRating(Integer rating) {
        if (rating == null) {
            return true; // Optional field
        }
        
        return rating >= 1 && rating <= 5;
    }

    /**
     * Validate text length
     */
    public static boolean isValidTextLength(String text, int maxLength) {
        if (text == null) {
            return true; // Null is valid for optional fields
        }
        
        return text.length() <= maxLength;
    }

    /**
     * Validate special requests text
     */
    public static boolean isValidSpecialRequests(String specialRequests) {
        return isValidTextLength(specialRequests, 500) &&
               (specialRequests == null || !containsProfanity(specialRequests));
    }

    /**
     * Validate room condition
     */
    public static boolean isValidRoomCondition(String condition) {
        if (condition == null || condition.trim().isEmpty()) {
            return true; // Optional field, default to "GOOD"
        }
        
        List<String> validConditions = List.of("EXCELLENT", "GOOD", "FAIR", "NEEDS_ATTENTION", "DAMAGED");
        return validConditions.contains(condition.trim().toUpperCase());
    }

    /**
     * Validate housekeeping priority
     */
    public static boolean isValidHousekeepingPriority(String priority) {
        if (priority == null || priority.trim().isEmpty()) {
            return true; // Optional field, default to "STANDARD"
        }
        
        List<String> validPriorities = List.of("LOW", "STANDARD", "HIGH", "URGENT");
        return validPriorities.contains(priority.trim().toUpperCase());
    }

    /**
     * Validate payment method
     */
    public static boolean isValidPaymentMethod(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            return true; // Optional field
        }
        
        List<String> validMethods = List.of("CREDIT_CARD", "DEBIT_CARD", "CASH", "BANK_TRANSFER");
        return validMethods.contains(paymentMethod.trim().toUpperCase());
    }

    /**
     * Validate charge type
     */
    public static boolean isValidChargeType(String chargeType) {
        if (chargeType == null || chargeType.trim().isEmpty()) {
            return false; // Required field for charges
        }
        
        List<String> validTypes = List.of("MINIBAR", "ROOM_SERVICE", "LAUNDRY", "TELEPHONE", 
                                        "PARKING", "LATE_CHECKOUT", "DAMAGES", "OTHER");
        return validTypes.contains(chargeType.trim().toUpperCase());
    }

    /**
     * Check if username is reserved
     */
    private static boolean isReservedUsername(String username) {
        List<String> reservedUsernames = List.of(
            "admin", "administrator", "root", "system", "guest", "test", 
            "api", "service", "support", "help", "info", "mail"
        );
        
        return reservedUsernames.contains(username.toLowerCase());
    }

    /**
     * Simple profanity filter (basic implementation)
     */
    private static boolean containsProfanity(String text) {
        if (text == null) return false;
        
        String lowerText = text.toLowerCase();
        List<String> profanityWords = List.of(
            // Add basic profanity words in Spanish/English
            "spam", "scam", "hack", "virus"
        );
        
        return profanityWords.stream().anyMatch(lowerText::contains);
    }

    /**
     * Validate ID format (MongoDB ObjectId or UUID)
     */
    public static boolean isValidId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = id.trim();
        
        // MongoDB ObjectId (24 hex characters)
        if (trimmed.matches("^[a-fA-F0-9]{24}$")) {
            return true;
        }
        
        // UUID format
        if (trimmed.matches("^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$")) {
            return true;
        }
        
        return false;
    }

    /**
     * Validate JSON object string (basic check)
     */
    public static boolean isValidJsonString(String json) {
        if (json == null || json.trim().isEmpty()) {
            return true; // Empty is valid
        }
        
        String trimmed = json.trim();
        return (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
               (trimmed.startsWith("[") && trimmed.endsWith("]"));
    }

    /**
     * Validate amenities list format
     */
    public static boolean isValidAmenitiesList(String amenities) {
        if (amenities == null || amenities.trim().isEmpty()) {
            return true; // Optional field
        }
        
        String[] amenityArray = amenities.split(",");
        if (amenityArray.length > 20) { // Max 20 amenities
            return false;
        }
        
        for (String amenity : amenityArray) {
            String trimmed = amenity.trim();
            if (trimmed.isEmpty() || trimmed.length() > 30) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Comprehensive validation result
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult failure(String message) {
            return new ValidationResult(false, message);
        }
    }
}