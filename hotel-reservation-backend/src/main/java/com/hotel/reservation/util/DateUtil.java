package com.hotel.reservation.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DateUtil {

    // Standard formatters
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    public static final DateTimeFormatter DISPLAY_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    
    // Hotel timezone (Colombia)
    public static final ZoneId HOTEL_TIMEZONE = ZoneId.of("America/Bogota");
    
    // Current system time (based on provided context)
    public static final LocalDateTime CURRENT_SYSTEM_TIME = LocalDateTime.of(2025, 11, 22, 4, 17, 34);

    /**
     * Get current date in hotel timezone
     */
    public static LocalDate getCurrentDate() {
        return CURRENT_SYSTEM_TIME.toLocalDate();
    }

    /**
     * Get current datetime in hotel timezone
     */
    public static LocalDateTime getCurrentDateTime() {
        return CURRENT_SYSTEM_TIME;
    }

    /**
     * Convert UTC datetime to hotel timezone
     */
    public static LocalDateTime utcToHotelTime(LocalDateTime utcDateTime) {
        if (utcDateTime == null) return null;
        
        return utcDateTime.atZone(ZoneOffset.UTC)
                .withZoneSameInstant(HOTEL_TIMEZONE)
                .toLocalDateTime();
    }

    /**
     * Convert hotel timezone to UTC
     */
    public static LocalDateTime hotelTimeToUtc(LocalDateTime hotelDateTime) {
        if (hotelDateTime == null) return null;
        
        return hotelDateTime.atZone(HOTEL_TIMEZONE)
                .withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();
    }

    /**
     * Parse date string safely
     */
    public static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        
        try {
            return LocalDate.parse(dateString.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse date: {} - {}", dateString, e.getMessage());
            return null;
        }
    }

    /**
     * Parse datetime string safely
     */
    public static LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }
        
        try {
            return LocalDateTime.parse(dateTimeString.trim(), DATETIME_FORMATTER);
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse datetime: {} - {}", dateTimeString, e.getMessage());
            return null;
        }
    }

    /**
     * Format date for display
     */
    public static String formatDisplayDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DISPLAY_DATE_FORMATTER);
    }

    /**
     * Format datetime for display
     */
    public static String formatDisplayDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DISPLAY_DATETIME_FORMATTER);
    }

    /**
     * Calculate number of nights between check-in and check-out
     */
    public static long calculateNights(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            return 0;
        }
        
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        return Math.max(0, nights); // Ensure non-negative
    }

    /**
     * Check if date is in the past
     */
    public static boolean isDateInPast(LocalDate date) {
        if (date == null) return false;
        return date.isBefore(getCurrentDate());
    }

    /**
     * Check if date is today
     */
    public static boolean isToday(LocalDate date) {
        if (date == null) return false;
        return date.equals(getCurrentDate());
    }

    /**
     * Check if date is tomorrow
     */
    public static boolean isTomorrow(LocalDate date) {
        if (date == null) return false;
        return date.equals(getCurrentDate().plusDays(1));
    }

    /**
     * Check if dates represent a valid stay period
     */
    public static boolean isValidStayPeriod(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            return false;
        }
        
        return checkOut.isAfter(checkIn) && 
               !isDateInPast(checkIn) && 
               calculateNights(checkIn, checkOut) <= 365; // Max 1 year stay
    }

    /**
     * Get check-in deadline time (usually 6 PM)
     */
    public static LocalDateTime getCheckInDeadline(LocalDate checkInDate) {
        if (checkInDate == null) return null;
        return checkInDate.atTime(18, 0); // 6:00 PM
    }

    /**
     * Get standard check-out time (usually 12 PM)
     */
    public static LocalDateTime getStandardCheckOutTime(LocalDate checkOutDate) {
        if (checkOutDate == null) return null;
        return checkOutDate.atTime(12, 0); // 12:00 PM
    }

    /**
     * Check if check-in is late
     */
    public static boolean isLateCheckIn(LocalDateTime checkInTime, LocalDate scheduledDate) {
        if (checkInTime == null || scheduledDate == null) return false;
        
        LocalDateTime deadline = getCheckInDeadline(scheduledDate);
        return checkInTime.isAfter(deadline);
    }

    /**
     * Check if check-out is late
     */
    public static boolean isLateCheckOut(LocalDateTime checkOutTime, LocalDate scheduledDate) {
        if (checkOutTime == null || scheduledDate == null) return false;
        
        LocalDateTime standardTime = getStandardCheckOutTime(scheduledDate);
        return checkOutTime.isAfter(standardTime);
    }

    /**
     * Generate date range between two dates
     */
    public static List<LocalDate> generateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return List.of();
        }
        
        return startDate.datesUntil(endDate.plusDays(1))
                .collect(Collectors.toList());
    }

    /**
     * Get age in years from birthdate
     */
    public static int calculateAge(LocalDate birthDate) {
        if (birthDate == null || birthDate.isAfter(getCurrentDate())) {
            return 0;
        }
        
        return Period.between(birthDate, getCurrentDate()).getYears();
    }

    /**
     * Format duration in human readable format
     */
    public static String formatDuration(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return "Unknown duration";
        
        Duration duration = Duration.between(start, end);
        
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        
        StringBuilder result = new StringBuilder();
        
        if (days > 0) {
            result.append(days).append(" day").append(days != 1 ? "s" : "");
        }
        
        if (hours > 0) {
            if (result.length() > 0) result.append(", ");
            result.append(hours).append(" hour").append(hours != 1 ? "s" : "");
        }
        
        if (minutes > 0 && days == 0) { // Only show minutes if less than a day
            if (result.length() > 0) result.append(", ");
            result.append(minutes).append(" minute").append(minutes != 1 ? "s" : "");
        }
        
        return result.length() > 0 ? result.toString() : "Less than a minute";
    }

    /**
     * Check if date falls within a season
     */
    public static boolean isDateInSeason(LocalDate date, LocalDate seasonStart, LocalDate seasonEnd) {
        if (date == null || seasonStart == null || seasonEnd == null) {
            return false;
        }
        
        // Handle seasons that cross year boundary
        if (seasonStart.isAfter(seasonEnd)) {
            return !date.isBefore(seasonStart) || !date.isAfter(seasonEnd);
        } else {
            return !date.isBefore(seasonStart) && !date.isAfter(seasonEnd);
        }
    }

    /**
     * Get next business day (Monday to Friday)
     */
    public static LocalDate getNextBusinessDay(LocalDate fromDate) {
        if (fromDate == null) return getCurrentDate().plusDays(1);
        
        LocalDate nextDay = fromDate.plusDays(1);
        
        while (nextDay.getDayOfWeek() == DayOfWeek.SATURDAY || 
               nextDay.getDayOfWeek() == DayOfWeek.SUNDAY) {
            nextDay = nextDay.plusDays(1);
        }
        
        return nextDay;
    }

    /**
     * Check if date is a weekend
     */
    public static boolean isWeekend(LocalDate date) {
        if (date == null) return false;
        
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    /**
     * Get start of day
     */
    public static LocalDateTime startOfDay(LocalDate date) {
        if (date == null) return null;
        return date.atStartOfDay();
    }

    /**
     * Get end of day
     */
    public static LocalDateTime endOfDay(LocalDate date) {
        if (date == null) return null;
        return date.atTime(23, 59, 59, 999999999);
    }

    /**
     * Check if check-in is allowed for the date
     */
    public static boolean isCheckInAllowed(LocalDate checkInDate) {
        LocalDate today = getCurrentDate();
        
        // Allow check-in for today and future dates
        return !checkInDate.isBefore(today);
    }

    /**
     * Check if check-out is allowed for the date
     */
    public static boolean isCheckOutAllowed(LocalDate checkOutDate) {
        LocalDate today = getCurrentDate();
        
        // Allow check-out for today and past dates (late check-outs)
        return !checkOutDate.isAfter(today.plusDays(1));
    }

    /**
     * Calculate cancellation deadline based on policy hours
     */
    public static LocalDateTime calculateCancellationDeadline(LocalDate checkInDate, int policyHours) {
        if (checkInDate == null) return null;
        
        // Standard check-in time is 3 PM
        LocalDateTime checkInTime = checkInDate.atTime(15, 0);
        return checkInTime.minusHours(policyHours);
    }

    /**
     * Check if current time is past cancellation deadline
     */
    public static boolean isPastCancellationDeadline(LocalDateTime deadline) {
        if (deadline == null) return false;
        return getCurrentDateTime().isAfter(deadline);
    }

    /**
     * Get reservation status based on dates and current time
     */
    public static String getReservationTimeStatus(LocalDate checkIn, LocalDate checkOut) {
        LocalDate today = getCurrentDate();
        
        if (checkOut.isBefore(today)) {
            return "PAST";
        } else if (checkIn.equals(today)) {
            return "TODAY_CHECKIN";
        } else if (checkOut.equals(today)) {
            return "TODAY_CHECKOUT";
        } else if (checkIn.isAfter(today)) {
            return "FUTURE";
        } else {
            return "CURRENT";
        }
    }
}