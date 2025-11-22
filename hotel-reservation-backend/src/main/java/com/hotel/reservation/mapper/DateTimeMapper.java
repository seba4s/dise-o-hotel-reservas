package com.hotel.reservation.mapper;

import org.mapstruct.Mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * MapStruct mapper for date and time conversions
 * Provides utility methods for consistent date/time formatting across all mappers
 */
@Mapper(componentModel = "spring")
public interface DateTimeMapper {

    /**
     * Standard date formatter for API responses
     */
    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Convert LocalDate to String
     */
    default String toString(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }

    /**
     * Convert String to LocalDate
     */
    default LocalDate toLocalDate(String date) {
        return date != null && !date.trim().isEmpty() ? 
            LocalDate.parse(date, DATE_FORMATTER) : null;
    }

    /**
     * Convert LocalDateTime to String
     */
    default String toString(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : null;
    }

    /**
     * Convert String to LocalDateTime
     */
    default LocalDateTime toLocalDateTime(String dateTime) {
        return dateTime != null && !dateTime.trim().isEmpty() ? 
            LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER) : null;
    }

    /**
     * Convert LocalDate to LocalDateTime (start of day)
     */
    default LocalDateTime toStartOfDay(LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }

    /**
     * Convert LocalDate to LocalDateTime (end of day)
     */
    default LocalDateTime toEndOfDay(LocalDate date) {
        return date != null ? date.atTime(23, 59, 59, 999999999) : null;
    }

    /**
     * Get current timestamp
     */
    default LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * Get current date
     */
    default LocalDate today() {
        return LocalDate.now();
    }
}