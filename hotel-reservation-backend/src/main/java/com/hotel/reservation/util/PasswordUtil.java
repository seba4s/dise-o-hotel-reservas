package com.hotel.reservation.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.regex.Pattern;

@Slf4j
@Component
public class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    
    // Password validation patterns
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*[0-9].*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
    
    // Common weak passwords
    private static final String[] COMMON_PASSWORDS = {
        "password", "123456", "password123", "admin", "qwerty", "letmein",
        "welcome", "monkey", "1234567890", "password1", "123456789", "guest"
    };

    /**
     * Validate password strength
     */
    public static PasswordStrength validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return new PasswordStrength(false, "Password cannot be empty", PasswordStrength.Level.VERY_WEAK);
        }

        String trimmedPassword = password.trim();
        int score = 0;
        StringBuilder feedback = new StringBuilder();

        // Check length
        if (trimmedPassword.length() < 6) {
            feedback.append("Password must be at least 6 characters long. ");
        } else if (trimmedPassword.length() >= 8) {
            score += 1;
            if (trimmedPassword.length() >= 12) {
                score += 1;
            }
        }

        // Check for uppercase letters
        if (UPPERCASE_PATTERN.matcher(trimmedPassword).matches()) {
            score += 1;
        } else {
            feedback.append("Add at least one uppercase letter. ");
        }

        // Check for lowercase letters
        if (LOWERCASE_PATTERN.matcher(trimmedPassword).matches()) {
            score += 1;
        } else {
            feedback.append("Add at least one lowercase letter. ");
        }

        // Check for digits
        if (DIGIT_PATTERN.matcher(trimmedPassword).matches()) {
            score += 1;
        } else {
            feedback.append("Add at least one number. ");
        }

        // Check for special characters
        if (SPECIAL_CHAR_PATTERN.matcher(trimmedPassword).matches()) {
            score += 1;
        } else {
            feedback.append("Add at least one special character (!@#$%^&*). ");
        }

        // Check for common passwords
        String lowerPassword = trimmedPassword.toLowerCase();
        for (String commonPassword : COMMON_PASSWORDS) {
            if (lowerPassword.contains(commonPassword)) {
                score = Math.max(0, score - 2);
                feedback.append("Avoid common passwords. ");
                break;
            }
        }

        // Check for repeated characters
        if (hasRepeatedCharacters(trimmedPassword)) {
            score = Math.max(0, score - 1);
            feedback.append("Avoid repeated characters. ");
        }

        // Check for sequential characters
        if (hasSequentialCharacters(trimmedPassword)) {
            score = Math.max(0, score - 1);
            feedback.append("Avoid sequential characters (abc, 123). ");
        }

        // Determine strength level
        PasswordStrength.Level level;
        if (score <= 1) {
            level = PasswordStrength.Level.VERY_WEAK;
        } else if (score <= 2) {
            level = PasswordStrength.Level.WEAK;
        } else if (score <= 4) {
            level = PasswordStrength.Level.MEDIUM;
        } else if (score <= 5) {
            level = PasswordStrength.Level.STRONG;
        } else {
            level = PasswordStrength.Level.VERY_STRONG;
        }

        // Check minimum requirements for validation
        boolean isValid = trimmedPassword.length() >= 6 &&
                         UPPERCASE_PATTERN.matcher(trimmedPassword).matches() &&
                         LOWERCASE_PATTERN.matcher(trimmedPassword).matches() &&
                         DIGIT_PATTERN.matcher(trimmedPassword).matches();

        String finalFeedback = feedback.length() > 0 ? feedback.toString().trim() : 
                              "Password meets security requirements.";

        return new PasswordStrength(isValid, finalFeedback, level);
    }

    /**
     * Generate a secure random password
     */
    public static String generateSecurePassword(int length) {
        if (length < 8) {
            length = 12; // Default to 12 characters for security
        }

        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        
        String allChars = upperCase + lowerCase + digits + specialChars;
        
        StringBuilder password = new StringBuilder();
        
        // Ensure at least one character from each category
        password.append(upperCase.charAt(RANDOM.nextInt(upperCase.length())));
        password.append(lowerCase.charAt(RANDOM.nextInt(lowerCase.length())));
        password.append(digits.charAt(RANDOM.nextInt(digits.length())));
        password.append(specialChars.charAt(RANDOM.nextInt(specialChars.length())));
        
        // Fill the rest randomly
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(RANDOM.nextInt(allChars.length())));
        }
        
        // Shuffle the password
        return shuffleString(password.toString());
    }

    /**
     * Generate a temporary password for password reset
     */
    public static String generateTemporaryPassword() {
        return generateSecurePassword(10);
    }

    /**
     * Check if password has repeated characters (3 or more in a row)
     */
    private static boolean hasRepeatedCharacters(String password) {
        for (int i = 0; i < password.length() - 2; i++) {
            if (password.charAt(i) == password.charAt(i + 1) && 
                password.charAt(i) == password.charAt(i + 2)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if password has sequential characters
     */
    private static boolean hasSequentialCharacters(String password) {
        String lowerPassword = password.toLowerCase();
        
        // Check for ascending sequences
        for (int i = 0; i < lowerPassword.length() - 2; i++) {
            char c1 = lowerPassword.charAt(i);
            char c2 = lowerPassword.charAt(i + 1);
            char c3 = lowerPassword.charAt(i + 2);
            
            if (c2 == c1 + 1 && c3 == c2 + 1) {
                return true;
            }
            
            // Check for descending sequences
            if (c2 == c1 - 1 && c3 == c2 - 1) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Shuffle a string randomly
     */
    private static String shuffleString(String input) {
        char[] chars = input.toCharArray();
        
        for (int i = chars.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        
        return new String(chars);
    }

    /**
     * Check if two passwords are similar (for password history)
     */
    public static boolean arePasswordsSimilar(String oldPassword, String newPassword, double threshold) {
        if (oldPassword == null || newPassword == null) return false;
        if (oldPassword.equals(newPassword)) return true;
        
        // Simple similarity check based on common characters
        String oldLower = oldPassword.toLowerCase();
        String newLower = newPassword.toLowerCase();
        
        int commonChars = 0;
        int totalChars = Math.max(oldLower.length(), newLower.length());
        
        for (int i = 0; i < Math.min(oldLower.length(), newLower.length()); i++) {
            if (oldLower.charAt(i) == newLower.charAt(i)) {
                commonChars++;
            }
        }
        
        double similarity = (double) commonChars / totalChars;
        return similarity >= threshold;
    }

    /**
     * Password strength result class
     */
    public static class PasswordStrength {
        private final boolean valid;
        private final String feedback;
        private final Level level;

        public PasswordStrength(boolean valid, String feedback, Level level) {
            this.valid = valid;
            this.feedback = feedback;
            this.level = level;
        }

        public boolean isValid() { return valid; }
        public String getFeedback() { return feedback; }
        public Level getLevel() { return level; }

        public enum Level {
            VERY_WEAK(0, "Very Weak", "#ff4444"),
            WEAK(1, "Weak", "#ff8800"),
            MEDIUM(2, "Medium", "#ffbb00"),
            STRONG(3, "Strong", "#88bb00"),
            VERY_STRONG(4, "Very Strong", "#00bb44");

            private final int score;
            private final String description;
            private final String color;

            Level(int score, String description, String color) {
                this.score = score;
                this.description = description;
                this.color = color;
            }

            public int getScore() { return score; }
            public String getDescription() { return description; }
            public String getColor() { return color; }
        }

        @Override
        public String toString() {
            return String.format("PasswordStrength{valid=%s, level=%s, feedback='%s'}", 
                               valid, level.getDescription(), feedback);
        }
    }
}