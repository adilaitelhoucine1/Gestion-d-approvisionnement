package com.tricol.gestionstock.dto.audit;

import com.tricol.gestionstock.entity.AuthAuditLog.AuthAction;
import com.tricol.gestionstock.entity.AuthAuditLog.AuditStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthAuditLogDTO {

    private Long id;
    private String username;
    private AuthAction action;
    private AuditStatus status;
    private String ipAddress;
    private String userAgent;
    private String details;
    private String errorMessage;
    private LocalDateTime actionDate;
    private String affectedResource;
    private String oldValue;
    private String newValue;
}

