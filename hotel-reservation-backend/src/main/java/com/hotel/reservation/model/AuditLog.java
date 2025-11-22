package com.hotel.reservation.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "audit_logs")
public class AuditLog {
    
    @Id
    private String id;
    
    @Indexed
    private String correlationId;
    
    @Indexed
    private String action; // CREATE, UPDATE, DELETE, LOGIN, LOGOUT, etc.
    
    @Indexed
    private String entityType; // USER, RESERVATION, ROOM, etc.
    
    @Indexed
    private String entityId;
    
    @Indexed
    private String userId;
    
    private String username;
    
    @Indexed
    private String userRole;
    
    private String ipAddress;
    private String userAgent;
    private String sessionId;
    
    private Map<String, Object> beforeData;
    private Map<String, Object> afterData;
    private Map<String, Object> metadata;
    
    private String notes;
    private String outcome; // SUCCESS, FAILURE, PARTIAL
    
    @Indexed
    @CreatedDate
    private LocalDateTime timestamp;
    
    // Immutability hash for audit integrity
    private String dataHash;
    
    /**
     * Generate hash for data integrity
     */
    public void generateHash() {
        String data = String.format("%s:%s:%s:%s:%s", 
            action, entityType, entityId, userId, timestamp);
        this.dataHash = org.apache.commons.codec.digest.DigestUtils.sha256Hex(data);
    }
}