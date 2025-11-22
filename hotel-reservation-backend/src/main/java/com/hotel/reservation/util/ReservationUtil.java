package com.hotel.reservation.util;

import com.hotel.reservation.model.Reservation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ReservationUtil {

    private static final Random RANDOM = new Random();
    private static final Pattern CONFIRMATION_NUMBER_PATTERN = Pattern.compile("^[A-Z]{4}[0-9]{8}$");
    
    // Tax rates by location/type
    public static final BigDecimal DEFAULT_TAX_RATE = BigDecimal.valueOf(0.19); // 19% IVA Colombia
    public static final BigDecimal SERVICE_FEE_RATE = BigDecimal.valueOf(0.10); // 10% service fee
    public static final BigDecimal CITY_TAX_PER_NIGHT = BigDecimal.valueOf(5000); // City tax per night
    
    /**
     * Generate unique confirmation number
     */
    public static String generateConfirmationNumber() {
        String prefix = generateRandomLetters(4);
        String timestamp = String.valueOf(System.currentTimeMillis() % 100000000L); // Last 8 digits
        
        return prefix + timestamp;
    }

    /**
     * Validate confirmation number format
     */
    public static boolean isValidConfirmationNumber(String confirmationNumber) {
        if (confirmationNumber == null || confirmationNumber.trim().isEmpty()) {
            return false;
        }
        
        return CONFIRMATION_NUMBER_PATTERN.matcher(confirmationNumber.trim().toUpperCase()).matches();
    }

    /**
     * Calculate reservation total with taxes and fees
     */
    public static BigDecimal calculateReservationTotal(BigDecimal baseAmount, int nights, 
                                                      boolean includeServiceFee, 
                                                      boolean includeCityTax) {
        if (baseAmount == null || baseAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = baseAmount;
        
        // Add service fee if applicable
        if (includeServiceFee) {
            BigDecimal serviceFee = baseAmount.multiply(SERVICE_FEE_RATE);
            total = total.add(serviceFee);
        }
        
        // Add city tax if applicable
        if (includeCityTax && nights > 0) {
            BigDecimal cityTax = CITY_TAX_PER_NIGHT.multiply(BigDecimal.valueOf(nights));
            total = total.add(cityTax);
        }
        
        // Add VAT/IVA
        BigDecimal tax = total.multiply(DEFAULT_TAX_RATE);
        total = total.add(tax);
        
        return total.setScale(0, RoundingMode.HALF_UP); // Round to nearest peso
    }

    /**
     * Calculate cancellation fee based on policy and timing
     */
    public static BigDecimal calculateCancellationFee(Reservation reservation) {
        if (reservation == null || reservation.getCancellationPolicy() == null) {
            return BigDecimal.ZERO;
        }

        Reservation.CancellationPolicy policy = reservation.getCancellationPolicy();
        LocalDateTime now = DateUtil.getCurrentDateTime();
        
        // Check if within free cancellation period
        if (policy.getFreeCancellationDeadline() != null && 
            now.isBefore(policy.getFreeCancellationDeadline())) {
            return BigDecimal.ZERO;
        }
        
        // Apply fixed fee if configured
        if (policy.getCancellationFeeFixed() != null) {
            return policy.getCancellationFeeFixed();
        }
        
        // Apply percentage fee if configured
        if (policy.getCancellationFeePercent() != null) {
            return reservation.getTotalAmount()
                    .multiply(policy.getCancellationFeePercent())
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        }
        
        return BigDecimal.ZERO;
    }

    /**
     * Calculate refund amount after cancellation
     */
    public static BigDecimal calculateRefundAmount(Reservation reservation) {
        if (reservation == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalPaid = reservation.getTotalAmount();
        BigDecimal cancellationFee = calculateCancellationFee(reservation);
        
        return totalPaid.subtract(cancellationFee).max(BigDecimal.ZERO);
    }

    /**
     * Check if reservation can be modified based on timing and policy
     */
    public static boolean canReservationBeModified(Reservation reservation) {
        if (reservation == null) {
            return false;
        }

        // Check reservation status
        if (!reservation.canBeModified()) {
            return false;
        }

        // Check if modification deadline has passed
        LocalDate checkInDate = reservation.getCheckInDate();
        LocalDate today = DateUtil.getCurrentDate();
        
        // Allow modifications up to 24 hours before check-in
        return checkInDate.isAfter(today) || checkInDate.equals(today);
    }

    /**
     * Check if reservation is eligible for early check-in
     */
    public static boolean isEligibleForEarlyCheckIn(Reservation reservation) {
        if (reservation == null || reservation.getStatus() != Reservation.ReservationStatus.CONFIRMED) {
            return false;
        }

        LocalDate today = DateUtil.getCurrentDate();
        LocalDate checkInDate = reservation.getCheckInDate();
        
        // Allow early check-in on the same day
        return checkInDate.equals(today) || checkInDate.equals(today.plusDays(1));
    }

    /**
     * Check if reservation is eligible for late check-out
     */
    public static boolean isEligibleForLateCheckOut(Reservation reservation) {
        if (reservation == null || reservation.getStatus() != Reservation.ReservationStatus.CHECKED_IN) {
            return false;
        }

        LocalDate today = DateUtil.getCurrentDate();
        LocalDate checkOutDate = reservation.getCheckOutDate();
        
        // Allow late check-out on checkout day
        return checkOutDate.equals(today);
    }

    /**
     * Calculate late check-out fee
     */
    public static BigDecimal calculateLateCheckOutFee(LocalDateTime actualCheckOut, LocalDate scheduledCheckOut) {
        if (actualCheckOut == null || scheduledCheckOut == null) {
            return BigDecimal.ZERO;
        }

        LocalDateTime standardCheckOut = DateUtil.getStandardCheckOutTime(scheduledCheckOut);
        
        if (actualCheckOut.isAfter(standardCheckOut)) {
            // Charge for late check-out (flat fee)
            return BigDecimal.valueOf(50000); // 50,000 COP
        }
        
        return BigDecimal.ZERO;
    }

    /**
     * Generate invoice number
     */
    public static String generateInvoiceNumber() {
        LocalDateTime now = DateUtil.getCurrentDateTime();
        String datePrefix = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomSuffix = String.format("%04d", RANDOM.nextInt(10000));
        
        return "INV-" + datePrefix + "-" + randomSuffix;
    }

    /**
     * Calculate room nights for pricing
     */
    public static int calculateRoomNights(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            return 0;
        }
        
        long nights = DateUtil.calculateNights(checkIn, checkOut);
        return Math.max(1, (int) nights); // Minimum 1 night for pricing
    }

    /**
     * Format reservation for display
     */
    public static String formatReservationSummary(Reservation reservation) {
        if (reservation == null) {
            return "No reservation data";
        }

        return String.format("%s - %s (%d nights) - %s %s - %s",
                reservation.getConfirmationNumber(),
                reservation.getRoom() != null ? reservation.getRoom().getRoomNumber() : "N/A",
                DateUtil.calculateNights(reservation.getCheckInDate(), reservation.getCheckOutDate()),
                formatCurrency(reservation.getTotalAmount()),
                reservation.getCurrency(),
                reservation.getStatus());
    }

    /**
     * Format currency amount
     */
    public static String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0";
        }
        
        return String.format("%,.0f", amount);
    }

    /**
     * Check if reservation is a no-show
     */
    public static boolean isNoShow(Reservation reservation) {
        if (reservation == null || reservation.getStatus() != Reservation.ReservationStatus.CONFIRMED) {
            return false;
        }

        LocalDate today = DateUtil.getCurrentDate();
        LocalDateTime checkInDeadline = DateUtil.getCheckInDeadline(reservation.getCheckInDate());
        LocalDateTime now = DateUtil.getCurrentDateTime();
        
        // Consider no-show if check-in date has passed and deadline is exceeded
        return reservation.getCheckInDate().isBefore(today) && 
               reservation.getActualCheckInTime() == null &&
               now.isAfter(checkInDeadline);
    }

    /**
     * Calculate occupancy rate for given period
     */
    public static BigDecimal calculateOccupancyRate(int occupiedRooms, int totalRooms) {
        if (totalRooms <= 0) {
            return BigDecimal.ZERO;
        }
        
        return BigDecimal.valueOf(occupiedRooms)
                .divide(BigDecimal.valueOf(totalRooms), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Generate random letters for confirmation number
     */
    private static String generateRandomLetters(int length) {
        StringBuilder result = new StringBuilder();
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        
        for (int i = 0; i < length; i++) {
            result.append(alphabet.charAt(RANDOM.nextInt(alphabet.length())));
        }
        
        return result.toString();
    }

    /**
     * Calculate average daily rate (ADR)
     */
    public static BigDecimal calculateAverageDailyRate(BigDecimal totalRevenue, int totalRoomNights) {
        if (totalRoomNights <= 0) {
            return BigDecimal.ZERO;
        }
        
        return totalRevenue.divide(BigDecimal.valueOf(totalRoomNights), 0, RoundingMode.HALF_UP);
    }

    /**
     * Calculate revenue per available room (RevPAR)
     */
    public static BigDecimal calculateRevPAR(BigDecimal totalRevenue, int availableRooms, int days) {
        if (availableRooms <= 0 || days <= 0) {
            return BigDecimal.ZERO;
        }
        
        int totalAvailableRoomNights = availableRooms * days;
        return totalRevenue.divide(BigDecimal.valueOf(totalAvailableRoomNights), 0, RoundingMode.HALF_UP);
    }

    /**
     * Validate guest capacity for room
     */
    public static boolean isValidGuestCapacity(int adults, int children, int roomCapacity, 
                                             boolean allowExtraBed, int maxExtraGuests) {
        int totalGuests = adults + children;
        
        if (totalGuests <= roomCapacity) {
            return true;
        }
        
        if (allowExtraBed) {
            return totalGuests <= (roomCapacity + maxExtraGuests);
        }
        
        return false;
    }

    /**
     * Generate correlation ID for tracking
     */
    public static String generateCorrelationId() {
        return "REQ_" + System.currentTimeMillis() + "_" + 
               UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Format duration between dates for display
     */
    public static String formatStayDuration(LocalDate checkIn, LocalDate checkOut) {
        long nights = DateUtil.calculateNights(checkIn, checkOut);
        
        if (nights == 0) {
            return "Same day";
        } else if (nights == 1) {
            return "1 night";
        } else {
            return nights + " nights";
        }
    }

    /**
     * Check if reservation dates overlap with another reservation
     */
    public static boolean doReservationDatesOverlap(LocalDate start1, LocalDate end1, 
                                                   LocalDate start2, LocalDate end2) {
        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return false;
        }
        
        // Check if one reservation ends before the other starts
        return !(end1.isBefore(start2) || end1.equals(start2) || 
                start1.isAfter(end2) || start1.equals(end2));
    }
}