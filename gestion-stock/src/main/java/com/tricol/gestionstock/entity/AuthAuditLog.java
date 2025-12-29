package com.tricol.gestionstock.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "auth_audit_logs", indexes = {
        @Index(name = "idx_auth_audit_user", columnList = "username"),
        @Index(name = "idx_auth_audit_action", columnList = "action"),
        @Index(name = "idx_auth_audit_date", columnList = "actionDate"),
        @Index(name = "idx_auth_audit_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuthAction action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuditStatus status;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 255)
    private String userAgent;

    @Column(length = 1000)
    private String details;

    @Column(length = 500)
    private String errorMessage;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime actionDate;

    @Column(length = 100)
    private String affectedResource;

    // For permission changes tracking
    @Column(length = 500)
    private String oldValue;

    @Column(length = 500)
    private String newValue;

    public enum AuthAction {
        LOGIN_SUCCESS,
        LOGIN_FAILURE,
        LOGOUT,
        REGISTER,
        TOKEN_REFRESH,
        PASSWORD_CHANGE,
        PERMISSION_GRANTED,
        PERMISSION_REVOKED,
        ROLE_ASSIGNED,
        ROLE_REMOVED,
        ACCOUNT_LOCKED,
        ACCOUNT_UNLOCKED,
        PASSWORD_RESET_REQUEST,
        PASSWORD_RESET_COMPLETE,
        TWO_FACTOR_ENABLED,
        TWO_FACTOR_DISABLED
    }

    public enum AuditStatus {
        SUCCESS,
        FAILURE,
        PENDING
    }
}

