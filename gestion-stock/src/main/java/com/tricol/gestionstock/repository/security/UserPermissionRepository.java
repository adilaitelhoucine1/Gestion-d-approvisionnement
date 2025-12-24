package com.tricol.gestionstock.repository.security;

import com.tricol.gestionstock.entity.security.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {

    List<UserPermission> findByUserId(Long userId);

    Optional<UserPermission> findByUserIdAndPermissionId(Long userId, Long permissionId);

    void deleteByUserIdAndPermissionId(Long userId, Long permissionId);
}

