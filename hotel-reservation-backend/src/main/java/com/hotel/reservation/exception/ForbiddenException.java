package com.hotel.reservation.exception;

import lombok.Getter;

import java.util.List;

/**
 * Exception thrown when access is denied due to insufficient permissions
 * Used for: role-based access control violations, ownership checks, etc.
 */
@Getter
public class ForbiddenException extends RuntimeException {

    private final String requiredRole;
    private final String currentRole;
    private final String resource;
    private final String action;

    public ForbiddenException(String message) {
        super(message);
        this.requiredRole = null;
        this.currentRole = null;
        this.resource = null;
        this.action = null;
    }

    public ForbiddenException(String message, String requiredRole, String currentRole) {
        super(message);
        this.requiredRole = requiredRole;
        this.currentRole = currentRole;
        this.resource = null;
        this.action = null;
    }

    public ForbiddenException(String message, String resource, String action, 
                             String requiredRole, String currentRole) {
        super(message);
        this.requiredRole = requiredRole;
        this.currentRole = currentRole;
        this.resource = resource;
        this.action = action;
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
        this.requiredRole = null;
        this.currentRole = null;
        this.resource = null;
        this.action = null;
    }

    /**
     * Static factory methods for common authorization issues
     */
    public static ForbiddenException insufficientRole(String requiredRole, String currentRole) {
        return new ForbiddenException(
            String.format("Access denied: requires %s role, current role: %s", requiredRole, currentRole),
            requiredRole,
            currentRole
        );
    }

    public static ForbiddenException insufficientRoleForResource(String resource, String action, 
                                                               String requiredRole, String currentRole) {
        return new ForbiddenException(
            String.format("Access denied: cannot %s %s with %s role, requires %s", 
                         action, resource, currentRole, requiredRole),
            resource,
            action,
            requiredRole,
            currentRole
        );
    }

    public static ForbiddenException notResourceOwner(String resource, String resourceId) {
        return new ForbiddenException(
            String.format("Access denied: you are not the owner of %s %s", resource, resourceId)
        );
    }

    public static ForbiddenException staffOnlyOperation(String operation) {
        return new ForbiddenException(
            String.format("Access denied: %s operation requires staff privileges", operation),
            "STAFF",
            null
        );
    }

    public static ForbiddenException adminOnlyOperation(String operation) {
        return new ForbiddenException(
            String.format("Access denied: %s operation requires admin privileges", operation),
            "ADMIN",
            null
        );
    }

    public static ForbiddenException cannotModifyReservation(String reservationId, String currentStatus) {
        return new ForbiddenException(
            String.format("Cannot modify reservation %s in status: %s", reservationId, currentStatus)
        );
    }

    public static ForbiddenException cannotCancelReservation(String reservationId, String currentStatus) {
        return new ForbiddenException(
            String.format("Cannot cancel reservation %s in status: %s", reservationId, currentStatus)
        );
    }

    public static ForbiddenException cannotAccessOtherUserData(String requestedUserId, String currentUserId) {
        return new ForbiddenException(
            String.format("Access denied: cannot access data for user %s", requestedUserId)
        );
    }
}
