package com.hotel.reservation.repository;

import com.hotel.reservation.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity
 * Handles authentication and user management queries
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    /**
     * Authentication queries
     */
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    /**
     * Existence checks for registration
     */
    Boolean existsByUsername(String username);
    
    Boolean existsByEmail(String email);
    
    @Query("{ $or: [ { 'username': ?0 }, { 'email': ?1 } ] }")
    Boolean existsByUsernameOrEmail(String username, String email);
    
    /**
     * Account verification queries
     */
    Optional<User> findByEmailVerificationToken(String token);
    
    @Query("{ 'emailVerificationToken': ?0, 'emailVerificationExpiry': { $gt: ?1 } }")
    Optional<User> findByEmailVerificationTokenAndNotExpired(String token, LocalDateTime now);
    
    /**
     * Password reset queries
     */
    Optional<User> findByPasswordResetToken(String token);
    
    @Query("{ 'passwordResetToken': ?0, 'passwordResetExpiry': { $gt: ?1 } }")
    Optional<User> findByPasswordResetTokenAndNotExpired(String token, LocalDateTime now);
    
    /**
     * Role-based queries
     */
    List<User> findByRole(User.Role role);
    
    @Query("{ 'role': { $in: ?0 } }")
    List<User> findByRoleIn(List<User.Role> roles);
    
    /**
     * Staff and admin queries for check-in/out operations
     */
    @Query("{ 'role': { $in: ['STAFF', 'ADMIN'] }, 'enabled': true, 'accountNonLocked': true }")
    List<User> findActiveStaffMembers();
    
    /**
     * Account status queries
     */
    @Query("{ 'enabled': true, 'accountNonLocked': true }")
    Page<User> findActiveUsers(Pageable pageable);
    
    @Query("{ 'accountNonLocked': false }")
    List<User> findLockedAccounts();
    
    @Query("{ 'lockedUntil': { $lt: ?0 } }")
    List<User> findAccountsWithExpiredLockouts(LocalDateTime now);
    
    /**
     * Login tracking queries
     */
    @Query("{ 'lastLogin': { $gte: ?0, $lte: ?1 } }")
    List<User> findUsersWithLastLoginBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("{ 'lastLogin': { $lt: ?0 } }")
    List<User> findInactiveUsersSince(LocalDateTime date);
    
    @Query("{ 'failedLoginAttempts': { $gte: ?0 } }")
    List<User> findUsersWithFailedLoginAttempts(Integer minAttempts);
    
    /**
     * Search queries for staff operations
     */
    @Query("{ $or: [ " +
           "{ 'firstName': { $regex: ?0, $options: 'i' } }, " +
           "{ 'lastName': { $regex: ?0, $options: 'i' } }, " +
           "{ 'email': { $regex: ?0, $options: 'i' } }, " +
           "{ 'username': { $regex: ?0, $options: 'i' } } " +
           "] }")
    List<User> searchUsers(String searchTerm);
    
    @Query("{ 'phone': { $regex: ?0 } }")
    List<User> findByPhoneContaining(String phone);
    
    @Query("{ 'country': ?0 }")
    List<User> findByCountry(String country);
    
    /**
     * Guest-specific queries for reservations
     */
    @Query("{ 'role': 'CLIENT', 'emailVerified': true }")
    List<User> findVerifiedGuests();
    
    @Query("{ 'firstName': { $regex: ?0, $options: 'i' }, 'lastName': { $regex: ?1, $options: 'i' } }")
    List<User> findByFirstNameAndLastNameLike(String firstName, String lastName);
    
    /**
     * Aggregation queries for statistics
     */
    @Aggregation(pipeline = {
        "{ $match: { 'createdAt': { $gte: ?0 } } }",
        "{ $group: { _id: '$role', count: { $sum: 1 } } }"
    })
    List<UserStatsProjection> getUserStatsByRole(LocalDateTime since);
    
    @Aggregation(pipeline = {
        "{ $match: { 'lastLogin': { $gte: ?0 } } }",
        "{ $group: { _id: { $dateToString: { format: '%Y-%m-%d', date: '$lastLogin' } }, count: { $sum: 1 } } }",
        "{ $sort: { '_id': 1 } }"
    })
    List<DailyLoginStatsProjection> getDailyLoginStats(LocalDateTime since);
    
    /**
     * Bulk operations for admin management
     */
    @Query("{ 'emailVerified': false, 'createdAt': { $lt: ?0 } }")
    List<User> findUnverifiedUsersOlderThan(LocalDateTime date);
    
    @Query("{ 'role': 'CLIENT', 'lastLogin': null, 'createdAt': { $lt: ?0 } }")
    List<User> findNeverLoggedInUsersOlderThan(LocalDateTime date);
    
    /**
     * Two-factor authentication queries
     */
    @Query("{ 'twoFactorEnabled': true }")
    List<User> findUsersWithTwoFactorEnabled();
    
    Optional<User> findByUsernameAndTwoFactorEnabled(String username, Boolean twoFactorEnabled);
    
    /**
     * Projection interfaces for aggregation results
     */
    interface UserStatsProjection {
        String getId(); // Role
        Long getCount();
    }
    
    interface DailyLoginStatsProjection {
        String getId(); // Date
        Long getCount();
    }
    
    /**
     * Custom query methods for complex business logic
     */
    
    /**
     * Find users who can perform check-in operations
     * Must be staff/admin, enabled, and not locked
     */
        @Query("""
                {
                    'role': { $in: ['STAFF', 'ADMIN'] },
                    'enabled': true,
                    'accountNonLocked': true,
                    '$or': [
                        { 'lockedUntil': null },
                        { 'lockedUntil': { $lt: ?0 } }
                    ]
                }
                """)
    List<User> findEligibleStaffForCheckIn(LocalDateTime currentTime);
    
    /**
     * Find users by partial document information for check-in lookup
     */
    @Query("{ 'nationalId': ?0 }")
    Optional<User> findByNationalId(String nationalId);
    
    /**
     * Find guest users for reservation lookup during check-in
     */
        @Query("""
                {
                    'role': 'CLIENT',
                    '$or': [
                        { 'email': { $regex: ?0, $options: 'i' } },
                        { 'lastName': { $regex: ?0, $options: 'i' } },
                        { 'nationalId': { $regex: ?0 } }
                    ]
                }
                """)
    List<User> findGuestsForCheckInLookup(String searchTerm);
    
    /**
     * Find users with upcoming reservations (for proactive communication)
     */
    @Query("{ " +
           "'role': 'CLIENT', " +
           "'preferences.emailNotifications': true " +
           "}")
    List<User> findGuestsWithEmailNotificationsEnabled();
}