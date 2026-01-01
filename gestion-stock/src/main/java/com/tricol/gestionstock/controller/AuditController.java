package com.tricol.gestionstock.controller;

import com.tricol.gestionstock.dto.audit.AuthAuditLogDTO;
import com.tricol.gestionstock.mapper.AuthAuditMapper;
import com.tricol.gestionstock.service.auth.AuthAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Audit log management APIs")
public class AuditController {

    private final AuthAuditService auditService;
    private final AuthAuditMapper auditMapper;

    @GetMapping("/user/{username}")
    @PreAuthorize("hasAnyAuthority('GERER_UTILISATEURS', 'VIEW_AUDIT_LOGS')")
    @Operation(summary = "Get audit logs by username", description = "Retrieve audit logs for a specific user")
    public ResponseEntity<Page<AuthAuditLogDTO>> getByUsername(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                auditService.getAuditLogsByUsername(username, PageRequest.of(page, size))
                        .map(auditMapper::toDTO)
        );
    }
}

