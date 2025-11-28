package com.niyiment.authservice.services;


import com.niyiment.authservice.domain.entity.AuditLog;
import com.niyiment.authservice.domain.entity.User;
import com.niyiment.authservice.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    @Async
    public void logSuccessfulLogin(User user) {
        logEvent(user, AuditLog.EventType.LOGIN_SUCCESS,
                "User logged in successfully", AuditLog.Status.SUCCESS);
    }

    @Async
    public void logFailedLogin(User user) {
        logEvent(user, AuditLog.EventType.LOGIN_FAILED,
                "Login attempt failed", AuditLog.Status.FAILURE);
    }

    @Async
    public void logRegisterEvent(User user) {
        logEvent(user, AuditLog.EventType.REGISTER,
                "User registered successfully", AuditLog.Status.SUCCESS);
    }

    @Async
    public void logAccountLocked(User user) {
        logEvent(user, AuditLog.EventType.ACCOUNT_LOCKED,
                "User account locked", AuditLog.Status.FAILURE);
    }

    @Async
    public void logLogoutEvent(User user) {
        logEvent(user, AuditLog.EventType.LOGOUT,
                "User logged out successfully", AuditLog.Status.SUCCESS);
    }

    @Async
    public void logTokenRefreshEvent(User user) {
        logEvent(user, AuditLog.EventType.TOKEN_REFRESH,
                "User token refreshed successfully", AuditLog.Status.SUCCESS);
    }

    @Async
    public void logRoleAssignment(User user, String roleName, Long assignedBy) {
        String details = String.format("Role '%s' assigned by user ID: %d", roleName, assignedBy);
        logEvent(user, AuditLog.EventType.ROLE_ASSIGNED, details, AuditLog.Status.SUCCESS);
    }

    @Async
    public void logSuspiciousActivity(Long userId, String description) {
        AuditLog auditLog = buildAuditLog(userId, null, AuditLog.EventType.SUSPICIOUS_ACTIVITY,
                description, AuditLog.Status.WARNING);
        auditLogRepository.save(auditLog);

        log.warn("Suspicious activity detected for user ID {}: {}", userId, description);
    }

    private void logEvent(User user, AuditLog.EventType eventType,
                          String details, AuditLog.Status status) {
        AuditLog auditLog = buildAuditLog(user.getId(), user.getUsername(),
                eventType, details, status);
        auditLogRepository.save(auditLog);
        log.info("Audit log saved for user: {}", user.getUsername());
    }

    private AuditLog buildAuditLog(Long userId, String username,
                                   AuditLog.EventType eventType,
                                   String details, AuditLog.Status status) {
        return AuditLog.builder()
                .userId(userId)
                .username(username)
                .eventType(eventType.name())
                .eventDetails(details)
                .ipAddress(extractIpAddress())
                .userAgent(extractUserAgent())
                .correlationId(MDC.get("correlationId"))
                .status(status.name())
                .build();
    }

    private String extractIpAddress() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) return null;

        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    private String extractUserAgent() {
        HttpServletRequest request = getCurrentRequest();
        return request == null ? null : request.getHeader("User-Agent");
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }
}

