package com.niyiment.authservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * AuditLog entity for tracking security events and user actions.
 * Immutable records for compliance and forensic analysis.
 */
@Entity
@Table(name = "audit_logs")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(length = 50)
    private String username;

    @Column(nullable = false, length = 50)
    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String eventDetails;

    @Column(length = 45)
    private String ipAddress;

    @Column(columnDefinition = "TEXT")
    private String userAgent;

    @Column(length = 100)
    private String correlationId;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    /**
     * Event types for audit logging.
     */
    public enum EventType {
        LOGIN_SUCCESS,
        LOGIN_FAILED,
        LOGOUT,
        REGISTER,
        PASSWORD_CHANGE,
        PASSWORD_RESET_REQUEST,
        PASSWORD_RESET_COMPLETE,
        ACCOUNT_LOCKED,
        ACCOUNT_UNLOCKED,
        EMAIL_VERIFICATION,
        ROLE_ASSIGNED,
        ROLE_REMOVED,
        PERMISSION_GRANTED,
        PERMISSION_REVOKED,
        TOKEN_REFRESH,
        SUSPICIOUS_ACTIVITY
    }

    /**
     * Status values for audit events.
     */
    public enum Status {
        SUCCESS,
        FAILURE,
        WARNING
    }
}