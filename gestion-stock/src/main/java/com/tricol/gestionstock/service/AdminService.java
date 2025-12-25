package com.tricol.gestionstock.service;

import com.tricol.gestionstock.dto.admin.*;

import java.util.List;

public interface AdminService {

    // User management
    List<UserResponseDTO> getAllUsers();
    UserResponseDTO getUserById(Long userId);
    UserResponseDTO getUserByUsername(String username);
    void deleteUser(Long userId);
    void enableUser(Long userId);
    void disableUser(Long userId);

    // Role management
    List<RoleDTO> getAllRoles();
    UserResponseDTO assignRoleToUser(Long userId, String roleName);
    UserResponseDTO removeRoleFromUser(Long userId);

    // Permission management
    List<PermissionDTO> getAllPermissions();
    List<PermissionDTO> getPermissionsByCategory(String category);
    UserResponseDTO updateUserPermission(Long userId, String permissionName, Boolean granted);
    UserResponseDTO removeUserCustomPermission(Long userId, String permissionName);
    List<UserPermissionDTO> getUserCustomPermissions(Long userId);
    List<PermissionDTO> getUserEffectivePermissions(Long userId);
}

