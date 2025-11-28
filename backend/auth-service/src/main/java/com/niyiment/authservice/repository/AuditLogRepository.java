package com.niyiment.authservice.repository;

import com.niyiment.authservice.domain.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository interface for AuditLog entity.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Find audit logs by user ID.
     */
    Page<AuditLog> findByUserId(Long userId, Pageable pageable);

    /**
     * Find audit logs by event type.
     */
    Page<AuditLog> findByEventType(String eventType, Pageable pageable);

    /**
     * Find audit logs by correlation ID.
     */
    List<AuditLog> findByCorrelationId(String correlationId);

    /**
     * Find audit logs within a time range.
     */
    @Query("SELECT al FROM AuditLog al WHERE al.createdAt BETWEEN :start AND :end")
    Page<AuditLog> findByDateRange(@Param("start") Instant start, 
                                    @Param("end") Instant end, 
                                    Pageable pageable);

    /**
     * Find failed login attempts for a user within a time window.
     */
    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.userId = :userId " +
           "AND al.eventType = 'LOGIN_FAILED' " +
           "AND al.createdAt > :since")
    long countFailedLoginAttempts(@Param("userId") Long userId, @Param("since") Instant since);

    /**
     * Find suspicious activities.
     */
    @Query("SELECT al FROM AuditLog al WHERE al.eventType = 'SUSPICIOUS_ACTIVITY' " +
           "AND al.createdAt > :since")
    List<AuditLog> findRecentSuspiciousActivities(@Param("since") Instant since);
}
