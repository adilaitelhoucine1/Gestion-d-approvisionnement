package com.tricol.gestionstock.service.auth;

import com.tricol.gestionstock.entity.AuthAuditLog;
import com.tricol.gestionstock.entity.AuthAuditLog.AuthAction;
import com.tricol.gestionstock.entity.AuthAuditLog.AuditStatus;
import com.tricol.gestionstock.repository.AuthAuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthAuditService {

    private final AuthAuditLogRepository auditLogRepository;

    // ==================== Logging Methods ====================

    @Async
    public void logLoginSuccess(String username) {
        log(username, AuthAction.LOGIN_SUCCESS, AuditStatus.SUCCESS, "User logged in successfully", null);
    }

    @Async
    public void logLoginFailure(String username, String reason) {
        log(username, AuthAction.LOGIN_FAILURE, AuditStatus.FAILURE, null, reason);
    }

    @Async
    public void logLogout(String username) {
        log(username, AuthAction.LOGOUT, AuditStatus.SUCCESS, "User logged out", null);
    }

    @Async
    public void logRegistration(String username) {
        log(username, AuthAction.REGISTER, AuditStatus.SUCCESS, "New user registered", null);
    }

    @Async
    public void logTokenRefresh(String username) {
        log(username, AuthAction.TOKEN_REFRESH, AuditStatus.SUCCESS, "Token refreshed", null);
    }

    @Async
    public void logPermissionChange(String username, AuthAction action, String resource, String oldValue, String newValue) {
        HttpServletRequest request = getCurrentRequest();

        AuthAuditLog auditLog = AuthAuditLog.builder()
                .username(username)
                .action(action)
                .status(AuditStatus.SUCCESS)
                .affectedResource(resource)
                .oldValue(oldValue)
                .newValue(newValue)
                .details("Performed by: " + getCurrentUsername())
                .ipAddress(extractIp(request))
                .userAgent(extractUserAgent(request))
                .build();

        save(auditLog);
    }

    // ==================== Query Method ====================

    @Transactional(readOnly = true)
    public Page<AuthAuditLog> getAuditLogsByUsername(String username, Pageable pageable) {
        return auditLogRepository.findByUsernameOrderByActionDateDesc(username, pageable);
    }

    // ==================== Private Helpers ====================

    private void log(String username, AuthAction action, AuditStatus status, String details, String error) {
        HttpServletRequest request = getCurrentRequest();

        AuthAuditLog auditLog = AuthAuditLog.builder()
                .username(username)
                .action(action)
                .status(status)
                .details(details)
                .errorMessage(error)
                .ipAddress(extractIp(request))
                .userAgent(extractUserAgent(request))
                .build();

        save(auditLog);
    }

    private void save(AuthAuditLog auditLog) {
        try {
            auditLogRepository.save(auditLog);
            log.info("Audit: {} - {} - {}", auditLog.getUsername(), auditLog.getAction(), auditLog.getStatus());
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage());
        }
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    private String extractIp(HttpServletRequest request) {
        if (request == null) return "UNKNOWN";
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        return (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) ? request.getRemoteAddr() : ip;
    }

    private String extractUserAgent(HttpServletRequest request) {
        return request != null ? request.getHeader("User-Agent") : "UNKNOWN";
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "SYSTEM";
    }
}

