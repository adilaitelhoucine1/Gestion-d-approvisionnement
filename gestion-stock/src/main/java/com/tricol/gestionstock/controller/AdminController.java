package com.tricol.gestionstock.controller;

import com.tricol.gestionstock.dto.admin.*;
import com.tricol.gestionstock.dto.auth.MessageResponseDTO;
import com.tricol.gestionstock.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Administration - Gestion des Utilisateurs", description = "Endpoints pour la gestion des utilisateurs, rôles et permissions (ADMIN uniquement)")
public class AdminController {

    private final AdminService adminService;


    @GetMapping
    @PreAuthorize("hasAuthority('GERER_UTILISATEURS')")
    @Operation(summary = "Lister tous les utilisateurs", description = "Requiert la permission GERER_UTILISATEURS")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        log.info("Request to get all users");
        List<UserResponseDTO> users = adminService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('GERER_UTILISATEURS')")
    @Operation(summary = "Obtenir un utilisateur par ID", description = "Requiert la permission GERER_UTILISATEURS")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long userId) {
        log.info("Request to get user with ID: {}", userId);
        UserResponseDTO user = adminService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("hasAuthority('GERER_UTILISATEURS')")
    @Operation(summary = "Obtenir un utilisateur par username", description = "Requiert la permission GERER_UTILISATEURS")
    public ResponseEntity<UserResponseDTO> getUserByUsername(@PathVariable String username) {
        log.info("Request to get user with username: {}", username);
        UserResponseDTO user = adminService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('GERER_UTILISATEURS')")
    @Operation(summary = "Supprimer un utilisateur", description = "Requiert la permission GERER_UTILISATEURS")
    public ResponseEntity<MessageResponseDTO> deleteUser(@PathVariable Long userId) {
        log.info("Request to delete user with ID: {}", userId);
        adminService.deleteUser(userId);
        return ResponseEntity.ok(new MessageResponseDTO("Utilisateur supprimé avec succès"));
    }

    @PutMapping("/{userId}/enable")
    @PreAuthorize("hasAuthority('GERER_UTILISATEURS')")
    @Operation(summary = "Activer un utilisateur", description = "Requiert la permission GERER_UTILISATEURS")
    public ResponseEntity<MessageResponseDTO> enableUser(@PathVariable Long userId) {
        log.info("Request to enable user with ID: {}", userId);
        adminService.enableUser(userId);
        return ResponseEntity.ok(new MessageResponseDTO("Utilisateur activé avec succès"));
    }

    @PutMapping("/{userId}/disable")
    @PreAuthorize("hasAuthority('GERER_UTILISATEURS')")
    @Operation(summary = "Désactiver un utilisateur", description = "Requiert la permission GERER_UTILISATEURS")
    public ResponseEntity<MessageResponseDTO> disableUser(@PathVariable Long userId) {
        log.info("Request to disable user with ID: {}", userId);
        adminService.disableUser(userId);
        return ResponseEntity.ok(new MessageResponseDTO("Utilisateur désactivé avec succès"));
    }



    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('GERER_UTILISATEURS')")
    @Operation(summary = "Lister tous les rôles disponibles", description = "Requiert la permission GERER_UTILISATEURS")
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        log.info("Request to get all roles");
        List<RoleDTO> roles = adminService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @PutMapping("/{userId}/role")
    @PreAuthorize("hasAuthority('GERER_UTILISATEURS')")
    @Operation(summary = "Assigner un rôle à un utilisateur", description = "Requiert la permission GERER_UTILISATEURS")
    public ResponseEntity<UserResponseDTO> assignRoleToUser(
            @PathVariable Long userId,
            @Valid @RequestBody AssignRoleRequestDTO request) {
        log.info("Request to assign role {} to user {}", request.getRoleName(), userId);
        UserResponseDTO user = adminService.assignRoleToUser(userId, request.getRoleName());
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{userId}/role")
    @PreAuthorize("hasAuthority('GERER_UTILISATEURS')")
    @Operation(summary = "Retirer le rôle d'un utilisateur", description = "Requiert la permission GERER_UTILISATEURS")
    public ResponseEntity<UserResponseDTO> removeRoleFromUser(@PathVariable Long userId) {
        log.info("Request to remove role from user {}", userId);
        UserResponseDTO user = adminService.removeRoleFromUser(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('GERER_UTILISATEURS')")
    @Operation(summary = "Lister toutes les permissions disponibles", description = "Requiert la permission GERER_UTILISATEURS")
    public ResponseEntity<List<PermissionDTO>> getAllPermissions() {
        log.info("Request to get all permissions");
        List<PermissionDTO> permissions = adminService.getAllPermissions();
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/permissions/category/{category}")
    @PreAuthorize("hasAuthority('GERER_UTILISATEURS')")
    @Operation(summary = "Lister les permissions par catégorie", description = "Requiert la permission GERER_UTILISATEURS")
    public ResponseEntity<List<PermissionDTO>> getPermissionsByCategory(@PathVariable String category) {
        log.info("Request to get permissions by category: {}", category);
        List<PermissionDTO> permissions = adminService.getPermissionsByCategory(category);
        return ResponseEntity.ok(permissions);
    }

    @PutMapping("/{userId}/permissions")
    @PreAuthorize("hasAuthority('GERER_UTILISATEURS')")
    @Operation(summary = "Modifier une permission personnalisée pour un utilisateur",
               description = "Permet d'accorder ou révoquer une permission spécifique, indépendamment du rôle. Requiert la permission GERER_UTILISATEURS")
    public ResponseEntity<UserResponseDTO> updateUserPermission(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserPermissionRequestDTO request) {
        log.info("Request to update permission {} for user {} to granted={}",
                request.getPermissionName(), userId, request.getGranted());
        UserResponseDTO user = adminService.updateUserPermission(
                userId, request.getPermissionName(), request.getGranted());
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{userId}/permissions/{permissionName}")
    @PreAuthorize("hasAuthority('GERER_UTILISATEURS')")
    @Operation(summary = "Supprimer une permission personnalisée d'un utilisateur",
               description = "L'utilisateur reviendra aux permissions par défaut de son rôle pour cette permission. Requiert la permission GERER_UTILISATEURS")
    public ResponseEntity<UserResponseDTO> removeUserCustomPermission(
            @PathVariable Long userId,
            @PathVariable String permissionName) {
        log.info("Request to remove custom permission {} from user {}", permissionName, userId);
        UserResponseDTO user = adminService.removeUserCustomPermission(userId, permissionName);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{userId}/permissions/custom")
    @PreAuthorize("hasAuthority('GERER_UTILISATEURS')")
    @Operation(summary = "Obtenir les permissions personnalisées d'un utilisateur",
               description = "Retourne uniquement les permissions qui ont été personnalisées pour cet utilisateur. Requiert la permission GERER_UTILISATEURS")
    public ResponseEntity<List<UserPermissionDTO>> getUserCustomPermissions(@PathVariable Long userId) {
        log.info("Request to get custom permissions for user {}", userId);
        List<UserPermissionDTO> permissions = adminService.getUserCustomPermissions(userId);
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/{userId}/permissions/effective")
    @PreAuthorize("hasAuthority('GERER_UTILISATEURS')")
    @Operation(summary = "Obtenir les permissions effectives d'un utilisateur",
               description = "Retourne toutes les permissions après application des règles du rôle et des personnalisations. Requiert la permission GERER_UTILISATEURS")
    public ResponseEntity<List<PermissionDTO>> getUserEffectivePermissions(@PathVariable Long userId) {
        log.info("Request to get effective permissions for user {}", userId);
        List<PermissionDTO> permissions = adminService.getUserEffectivePermissions(userId);
        return ResponseEntity.ok(permissions);
    }
}

