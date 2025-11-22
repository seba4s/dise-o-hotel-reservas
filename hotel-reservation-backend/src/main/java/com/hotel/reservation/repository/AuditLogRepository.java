package com.hotel.reservation.repository;

import com.hotel.reservation.model.AuditLog;
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
 * Repository for AuditLog entity
 * Handles audit trail queries for security and compliance
 */
@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {

    /**
     * Basic audit queries
     */
    Optional<AuditLog> findByCorrelationId(String correlationId);
    
    List<AuditLog> findByCorrelationIdOrderByTimestampAsc(String correlationId);
    
    /**
     * User activity tracking
     */
    List<AuditLog> findByUserId(String userId);
    
    List<AuditLog> findByUserIdOrderByTimestampDesc(String userId);
    
    Page<AuditLog> findByUserId(String userId, Pageable pageable);
    
    @Query("{ 'username': { $regex: ?0, $options: 'i' } }")
    List<AuditLog> findByUsernameLike(String username);
    
    /**
     * Action-based queries
     */
    List<AuditLog> findByAction(String action);
    
    @Query("{ 'action': { $in: ?0 } }")
    List<AuditLog> findByActionIn(List<String> actions);
    
    /**
     * Entity-specific audit trails
     */
    @Query("{ 'entityType': ?0, 'entityId': ?1 }")
    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampAsc(String entityType, String entityId);
    
    List<AuditLog> findByEntityType(String entityType);
    
    /**
     * Time-based queries
     */
    @Query("{ 'timestamp': { $gte: ?0, $lte: ?1 } }")
    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("{ 'timestamp': { $gte: ?0 } }")
    Page<AuditLog> findByTimestampAfter(LocalDateTime after, Pageable pageable);
    
    /**
     * Security-related queries
     */
    @Query("{ 'action': { $in: ['LOGIN', 'LOGOUT', 'LOGIN_FAILED'] } }")
    List<AuditLog> findAuthenticationEvents();
    
    @Query("{ 'action': 'LOGIN_FAILED', 'timestamp': { $gte: ?0 } }")
    List<AuditLog> findFailedLoginAttemptsSince(LocalDateTime since);
    
    @Query("{ 'ipAddress': ?0, 'action': 'LOGIN_FAILED', 'timestamp': { $gte: ?1 } }")
    List<AuditLog> findFailedLoginAttemptsFromIP(String ipAddress, LocalDateTime since);
    
    /**
     * Role-based access queries
     */
    List<AuditLog> findByUserRole(String userRole);
    
    @Query("{ 'userRole': { $in: ['ADMIN', 'STAFF'] } }")
    List<AuditLog> findStaffAndAdminActions();
    
    /**
     * Business operation audit queries
     */
    @Query("{ 'action': { $in: ['RESERVATION_CREATE', 'RESERVATION_UPDATE', 'RESERVATION_CANCEL'] } }")
    List<AuditLog> findReservationActions();
    
    @Query("{ 'action': { $in: ['CHECKIN_PROCESS', 'CHECKOUT_PROCESS'] } }")
    List<AuditLog> findCheckInCheckOutActions();
    
    /**
     * Compliance and reporting queries
     */
    @Aggregation(pipeline = {
        "{ $match: { 'timestamp': { $gte: ?0 } } }",
        "{ $group: { _id: '$action', count: { $sum: 1 } } }",
        "{ $sort: { 'count': -1 } }"
    })
    List<ActionStatsProjection> getActionStatistics(LocalDateTime since);
    
    @Aggregation(pipeline = {
        "{ $match: { 'timestamp': { $gte: ?0 } } }",
        "{ $group: { _id: '$userRole', count: { $sum: 1 } } }"
    })
    List<RoleActivityStatsProjection> getRoleActivityStatistics(LocalDateTime since);
    
    /**
     * Projection interfaces
     */
    interface ActionStatsProjection {
        String getId(); // Action
        Long getCount();
    }
    
    interface RoleActivityStatsProjection {
        String getId(); // User role
        Long getCount();
    }
    
    /**
     * Data retention queries
     */
    @Query("{ 'timestamp': { $lt: ?0 } }")
    List<AuditLog> findLogsOlderThan(LocalDateTime cutoffDate);
    
    /**
     * IP address tracking
     */
    List<AuditLog> findByIpAddress(String ipAddress);
    
    @Query("{ 'ipAddress': ?0, 'timestamp': { $gte: ?1, $lte: ?2 } }")
    List<AuditLog> findByIpAddressAndTimestampBetween(String ipAddress, LocalDateTime start, LocalDateTime end);
    
    /**
     * Session tracking
     */
    List<AuditLog> findBySessionId(String sessionId);
    
    @Query("{ 'sessionId': ?0 }")
    List<AuditLog> findSessionActivity(String sessionId);
}