package com.hotel.reservation.repository;

import com.hotel.reservation.model.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Reservation entity
 * Handles queries for HU004 (Create), HU009 (Check-in), HU010 (Check-out)
 */
@Repository
public interface ReservationRepository extends MongoRepository<Reservation, String> {

		/**
		 * Basic reservation queries
		 */
		Optional<Reservation> findByConfirmationNumber(String confirmationNumber);

		Boolean existsByConfirmationNumber(String confirmationNumber);

		/**
		 * Guest-specific queries
		 */
		List<Reservation> findByGuestId(String guestId);

		List<Reservation> findByGuestIdOrderByCreatedAtDesc(String guestId);

		@Query("{ 'guest.id': ?0, 'status': { $in: ?1 } }")
		List<Reservation> findByGuestIdAndStatusIn(String guestId, List<Reservation.ReservationStatus> statuses);

		/**
		 * Room-specific queries for availability checking (HU001/HU004)
		 */
		List<Reservation> findByRoomId(String roomId);

		/**
		 * Critical query: Find conflicting reservations for availability check
		 * Used in HU001 and HU004 to prevent double bookings
		 */
		@Query("""
				{
					'room.id': ?0,
					'$or': [
						{ 'checkInDate': { $lte: ?2 }, 'checkOutDate': { $gt: ?1 } },
						{ 'checkInDate': { $lt: ?2 }, 'checkOutDate': { $gte: ?1 } }
					],
					'status': { $in: ['CONFIRMED', 'CHECKED_IN', 'PRE_RESERVATION'] }
				}
				""")
		List<Reservation> findConflictingReservations(String roomId, LocalDate startDate, LocalDate endDate);

		/**
		 * More comprehensive conflict check including edge cases
		 */
		@Query("""
				{
					'room.id': ?0,
					'checkInDate': { $lt: ?2 },
					'checkOutDate': { $gt: ?1 },
					'status': { $nin: ['CANCELLED', 'CHECKED_OUT'] },
					'id': { $ne: ?3 }
				}
				""")
		List<Reservation> findConflictingReservationsExcludingId(
				String roomId,
				LocalDate startDate,
				LocalDate endDate,
				String excludeReservationId
		);

		/**
		 * HU009: Check-in queries
		 */

		/**
		 * Find reservations scheduled for check-in today
		 */
		@Query("{ 'checkInDate': ?0, 'status': 'CONFIRMED' }")
		List<Reservation> findReservationsForCheckInToday(LocalDate date);

		/**
		 * Find reservations for check-in by date range
		 */
		@Query("""
				{
					'checkInDate': { $gte: ?0, $lte: ?1 },
					'status': 'CONFIRMED'
				}
				""")
		List<Reservation> findReservationsForCheckIn(LocalDate startDate, LocalDate endDate);

		/**
		 * Find overdue check-ins (guests who didn't show up)
		 */
		@Query("""
				{
					'checkInDate': { $lt: ?0 },
					'status': 'CONFIRMED',
					'actualCheckInTime': null
				}
				""")
		List<Reservation> findOverdueCheckIns(LocalDate currentDate);

		/**
		 * Search reservations for check-in by guest information
		 */
		@Query("""
				{
					'status': 'CONFIRMED',
					'checkInDate': { $gte: ?1, $lte: ?2 },
					'$or': [
						{ 'guest.email': { $regex: ?0, $options: 'i' } },
						{ 'guest.lastName': { $regex: ?0, $options: 'i' } },
						{ 'confirmationNumber': { $regex: ?0, $options: 'i' } }
					]
				}
				""")
		List<Reservation> searchReservationsForCheckIn(String searchTerm, LocalDate startDate, LocalDate endDate);

		/**
		 * HU010: Check-out queries
		 */

		/**
		 * Find reservations scheduled for check-out today
		 */
		@Query("{ 'checkOutDate': ?0, 'status': 'CHECKED_IN' }")
		List<Reservation> findReservationsForCheckOutToday(LocalDate date);

		/**
		 * Find reservations for check-out by date range
		 */
		@Query("""
				{
					'checkOutDate': { $gte: ?0, $lte: ?1 },
					'status': 'CHECKED_IN'
				}
				""")
		List<Reservation> findReservationsForCheckOut(LocalDate startDate, LocalDate endDate);

		/**
		 * Find late check-outs (past scheduled checkout time)
		 */
		@Query("""
				{
					'checkOutDate': { $lt: ?0 },
					'status': 'CHECKED_IN',
					'actualCheckOutTime': null
				}
				""")
		List<Reservation> findLateCheckOuts(LocalDate currentDate);

		/**
		 * Find current guest in a specific room
		 */
		@Query("""
				{
					'room.roomNumber': ?0,
					'status': 'CHECKED_IN',
					'actualCheckOutTime': null
				}
				""")
		Optional<Reservation> findCurrentGuestInRoom(String roomNumber);

		/**
		 * Status-based queries
		 */
		List<Reservation> findByStatus(Reservation.ReservationStatus status);

		List<Reservation> findByStatusOrderByCreatedAtDesc(Reservation.ReservationStatus status);

		@Query("{ 'status': { $in: ?0 } }")
		List<Reservation> findByStatusIn(List<Reservation.ReservationStatus> statuses);

		/**
		 * Date-based queries
		 */
		@Query("{ 'createdAt': { $gte: ?0, $lte: ?1 } }")
		List<Reservation> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

		@Query("{ 'checkInDate': { $gte: ?0, $lte: ?1 } }")
		List<Reservation> findByCheckInDateBetween(LocalDate start, LocalDate end);

		@Query("{ 'checkOutDate': { $gte: ?0, $lte: ?1 } }")
		List<Reservation> findByCheckOutDateBetween(LocalDate start, LocalDate end);

		/**
		 * Financial queries
		 */
		@Query("{ 'totalAmount': { $gte: ?0, $lte: ?1 } }")
		List<Reservation> findByTotalAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);

		@Query("{ 'paymentStatus': ?0 }")
		List<Reservation> findByPaymentStatus(String paymentStatus);

		@Query("{ 'paymentStatus': { $in: ['PENDING', 'FAILED'] } }")
		List<Reservation> findReservationsWithPendingPayment();

		/**
		 * Cancellation queries
		 */
		@Query("{ 'status': 'CANCELLED', 'cancelledAt': { $gte: ?0, $lte: ?1 } }")
		List<Reservation> findCancelledReservationsBetween(LocalDateTime start, LocalDateTime end);

		@Query("{ 'status': 'CANCELLED', 'cancelledBy': ?0 }")
		List<Reservation> findReservationsCancelledBy(String cancelledBy);

		/**
		 * Operational queries for staff
		 */

		/**
		 * Find all active reservations (confirmed or checked in)
		 */
		@Query("{ 'status': { $in: ['CONFIRMED', 'CHECKED_IN'] } }")
		List<Reservation> findActiveReservations();

		/**
		 * Find reservations that need attention (overdue, pending payment, etc.)
		 */
		@Query("""
				{
					'$or': [
						{ 'checkInDate': { $lt: ?0 }, 'status': 'CONFIRMED', 'actualCheckInTime': null },
						{ 'checkOutDate': { $lt: ?0 }, 'status': 'CHECKED_IN', 'actualCheckOutTime': null },
						{ 'paymentStatus': { $in: ['PENDING', 'FAILED'] } }
					]
				}
				""")
		List<Reservation> findReservationsNeedingAttention(LocalDate currentDate);

		/**
		 * Check-in/out staff tracking queries
		 */
		@Query("{ 'checkInStaff': ?0, 'actualCheckInTime': { $gte: ?1, $lte: ?2 } }")
		List<Reservation> findCheckInsProcessedByStaff(String staffUsername, LocalDateTime start, LocalDateTime end);

		@Query("{ 'checkOutStaff': ?0, 'actualCheckOutTime': { $gte: ?1, $lte: ?2 } }")
		List<Reservation> findCheckOutsProcessedByStaff(String staffUsername, LocalDateTime start, LocalDateTime end);

		/**
		 * Advanced search for staff operations
		 */
		@Query("""
				{
					'$or': [
						{ 'confirmationNumber': { $regex: ?0, $options: 'i' } },
						{ 'guest.firstName': { $regex: ?0, $options: 'i' } },
						{ 'guest.lastName': { $regex: ?0, $options: 'i' } },
						{ 'guest.email': { $regex: ?0, $options: 'i' } },
						{ 'room.roomNumber': { $regex: ?0, $options: 'i' } }
					]
				}
				""")
		List<Reservation> searchReservations(String searchTerm);

		/**
		 * Aggregation queries for analytics and reporting
		 */
		@Aggregation(pipeline = {
				"{ $match: { 'createdAt': { $gte: ?0 } } }",
				"{ $group: { _id: '$status', count: { $sum: 1 }, totalAmount: { $sum: '$totalAmount' } } }"
		})
		List<ReservationStatusStatsProjection> getReservationStatusStatistics(LocalDateTime since);

		@Aggregation(pipeline = {
				"{ $match: { 'actualCheckInTime': { $gte: ?0, $lte: ?1 } } }",
				"{ $group: { " +
				"    _id: { $dateToString: { format: '%Y-%m-%d', date: '$actualCheckInTime' } }, " +
				"    count: { $sum: 1 }, " +
				"    revenue: { $sum: '$totalAmount' } " +
				"  } }",
				"{ $sort: { '_id': 1 } }"
		})
		List<DailyRevenueStatsProjection> getDailyCheckInStatistics(LocalDateTime start, LocalDateTime end);

		@Aggregation(pipeline = {
				"{ $match: { 'status': { $in: ['CHECKED_IN', 'CHECKED_OUT'] } } }",
				"{ $group: { " +
				"    _id: '$room.roomType', " +
				"    count: { $sum: 1 }, " +
				"    avgAmount: { $avg: '$totalAmount' }, " +
				"    totalRevenue: { $sum: '$totalAmount' } " +
				"  } }",
				"{ $sort: { 'totalRevenue': -1 } }"
		})
		List<RoomTypePerformanceProjection> getRoomTypePerformanceStatistics();

		/**
		 * Projection interfaces for aggregation results
		 */
		interface ReservationStatusStatsProjection {
				String getId(); // Status
				Long getCount();
				Double getTotalAmount();
		}

		interface DailyRevenueStatsProjection {
				String getId(); // Date
				Long getCount();
				Double getRevenue();
		}

		interface RoomTypePerformanceProjection {
				String getId(); // Room type
				Long getCount();
				Double getAvgAmount();
				Double getTotalRevenue();
		}

		/**
		 * Business intelligence queries
		 */

		/**
		 * Find reservations with high additional charges (potential upselling)
		 */
		@Query("""
				{
					'additionalCharges.0': { $exists: true },
					'status': { $in: ['CHECKED_IN', 'CHECKED_OUT'] }
				}
				""")
		List<Reservation> findReservationsWithAdditionalCharges();

		/**
		 * Find repeat guests
		 */
		@Aggregation(pipeline = {
				"{ $group: { _id: '$guest.id', count: { $sum: 1 }, reservations: { $push: '$$ROOT' } } }",
				"{ $match: { 'count': { $gt: 1 } } }"
		})
		List<RepeatGuestProjection> findRepeatGuests();

		interface RepeatGuestProjection {
				String getId(); // Guest ID
				Long getCount();
				List<Reservation> getReservations();
		}

		/**
		 * Find no-shows for pattern analysis
		 */
		@Query("""
				{
					'status': 'CONFIRMED',
					'checkInDate': { $lt: ?0 },
					'actualCheckInTime': null
				}
				""")
		List<Reservation> findNoShowReservations(LocalDate beforeDate);

		/**
		 * Seasonal booking patterns
		 */
		@Aggregation(pipeline = {
				"{ $match: { 'status': { $in: ['CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT'] } } }",
				"{ $group: { " +
				"    _id: { $month: '$checkInDate' }, " +
				"    count: { $sum: 1 }, " +
				"    avgAmount: { $avg: '$totalAmount' } " +
				"  } }",
				"{ $sort: { '_id': 1 } }"
		})
		List<MonthlyBookingStatsProjection> getMonthlyBookingPatterns();

		interface MonthlyBookingStatsProjection {
				Integer getId(); // Month number
				Long getCount();
				Double getAvgAmount();
		}

		/**
		 * Paginated queries for staff interfaces
		 */
		Page<Reservation> findByStatusOrderByCreatedAtDesc(Reservation.ReservationStatus status, Pageable pageable);

		@Query("{ 'checkInDate': { $gte: ?0, $lte: ?1 } }")
		Page<Reservation> findByCheckInDateBetween(LocalDate start, LocalDate end, Pageable pageable);

		@Query("{ 'guest.id': ?0 }")
		Page<Reservation> findByGuestId(String guestId, Pageable pageable);
}

