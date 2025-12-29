package com.tricol.gestionstock.repository;

import com.tricol.gestionstock.entity.AuthAuditLog;
import com.tricol.gestionstock.entity.AuthAuditLog.AuthAction;
import com.tricol.gestionstock.entity.AuthAuditLog.AuditStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuthAuditLogRepository extends JpaRepository<AuthAuditLog, Long> {

    // Find by username
    Page<AuthAuditLog> findByUsernameOrderByActionDateDesc(String username, Pageable pageable);

    // Find by action type
    Page<AuthAuditLog> findByActionOrderByActionDateDesc(AuthAction action, Pageable pageable);

    // Find by status
    Page<AuthAuditLog> findByStatusOrderByActionDateDesc(AuditStatus status, Pageable pageable);

    // Find by date range
    Page<AuthAuditLog> findByActionDateBetweenOrderByActionDateDesc(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    // Find login failures by username
    @Query("SELECT a FROM AuthAuditLog a WHERE a.username = :username " +
           "AND a.action = 'LOGIN_FAILURE' " +
           "AND a.actionDate > :since " +
           "ORDER BY a.actionDate DESC")
    List<AuthAuditLog> findRecentLoginFailures(
            @Param("username") String username,
            @Param("since") LocalDateTime since
    );

    // Count failed login attempts
    @Query("SELECT COUNT(a) FROM AuthAuditLog a WHERE a.username = :username " +
           "AND a.action = 'LOGIN_FAILURE' " +
           "AND a.actionDate > :since")
    long countRecentLoginFailures(
            @Param("username") String username,
            @Param("since") LocalDateTime since
    );

    // Find permission changes
    @Query("SELECT a FROM AuthAuditLog a WHERE a.action IN ('PERMISSION_GRANTED', 'PERMISSION_REVOKED', 'ROLE_ASSIGNED', 'ROLE_REMOVED') " +
           "ORDER BY a.actionDate DESC")
    Page<AuthAuditLog> findPermissionChanges(Pageable pageable);

    // Search audit logs
    @Query("SELECT a FROM AuthAuditLog a WHERE " +
           "(:username IS NULL OR a.username LIKE %:username%) AND " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:ipAddress IS NULL OR a.ipAddress = :ipAddress) AND " +
           "(:startDate IS NULL OR a.actionDate >= :startDate) AND " +
           "(:endDate IS NULL OR a.actionDate <= :endDate) " +
           "ORDER BY a.actionDate DESC")
    Page<AuthAuditLog> searchAuditLogs(
            @Param("username") String username,
            @Param("action") AuthAction action,
            @Param("status") AuditStatus status,
            @Param("ipAddress") String ipAddress,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // Get user activity summary
    @Query("SELECT a.action, COUNT(a) FROM AuthAuditLog a " +
           "WHERE a.username = :username " +
           "AND a.actionDate > :since " +
           "GROUP BY a.action")
    List<Object[]> getUserActivitySummary(
            @Param("username") String username,
            @Param("since") LocalDateTime since
    );
}

