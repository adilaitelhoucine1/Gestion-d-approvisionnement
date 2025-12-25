package com.tricol.gestionstock.service.impl;

import com.tricol.gestionstock.dto.admin.*;
import com.tricol.gestionstock.entity.security.*;
import com.tricol.gestionstock.exception.ResourceNotFoundException;
import com.tricol.gestionstock.mapper.PermissionMapper;
import com.tricol.gestionstock.mapper.RoleMapper;
import com.tricol.gestionstock.mapper.UserMapper;
import com.tricol.gestionstock.mapper.UserPermissionMapper;
import com.tricol.gestionstock.repository.security.*;
import com.tricol.gestionstock.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminServiceImpl implements AdminService {

    private final UserAppRepository userRepository;
    private final RoleAppRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserPermissionRepository userPermissionRepository;

    // Mappers
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final UserPermissionMapper userPermissionMapper;


    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        log.info("Fetching all users");
        return userMapper.toDTOList(userRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long userId) {
        log.info("Fetching user with ID: {}", userId);
        UserApp user = findUserById(userId);
        return userMapper.toDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserByUsername(String username) {
        log.info("Fetching user with username: {}", username);
        UserApp user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec le username: " + username));
        return userMapper.toDTO(user);
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("Deleting user with ID: {}", userId);
        UserApp user = findUserById(userId);
        userRepository.delete(user);
        log.info("User deleted successfully: {}", userId);
    }

    @Override
    public void enableUser(Long userId) {
        log.info("Enabling user with ID: {}", userId);
        UserApp user = findUserById(userId);
        user.setEnabled(true);
        userRepository.save(user);
        log.info("User enabled successfully: {}", userId);
    }

    @Override
    public void disableUser(Long userId) {
        log.info("Disabling user with ID: {}", userId);
        UserApp user = findUserById(userId);
        user.setEnabled(false);
        userRepository.save(user);
        log.info("User disabled successfully: {}", userId);
    }



    @Override
    @Transactional(readOnly = true)
    public List<RoleDTO> getAllRoles() {
        log.info("Fetching all roles");
        return roleMapper.toDTOList(roleRepository.findAll());
    }

    @Override
    public UserResponseDTO assignRoleToUser(Long userId, String roleName) {
        log.info("Assigning role {} to user {}", roleName, userId);
        UserApp user = findUserById(userId);
        RoleApp role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Rôle non trouvé: " + roleName));

        user.setRole(role);
        UserApp savedUser = userRepository.save(user);
        log.info("Role {} assigned to user {} successfully", roleName, userId);
        return userMapper.toDTO(savedUser);
    }

    @Override
    public UserResponseDTO removeRoleFromUser(Long userId) {
        log.info("Removing role from user {}", userId);
        UserApp user = findUserById(userId);
        user.setRole(null);
        UserApp savedUser = userRepository.save(user);
        log.info("Role removed from user {} successfully", userId);
        return userMapper.toDTO(savedUser);
    }


    @Override
    @Transactional(readOnly = true)
    public List<PermissionDTO> getAllPermissions() {
        log.info("Fetching all permissions");
        return permissionMapper.toDTOList(permissionRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionDTO> getPermissionsByCategory(String category) {
        log.info("Fetching permissions by category: {}", category);
        return permissionMapper.toDTOList(permissionRepository.findByCategory(category));
    }

    @Override
    public UserResponseDTO updateUserPermission(Long userId, String permissionName, Boolean granted) {
        log.info("Updating permission {} for user {} to granted={}", permissionName, userId, granted);

        UserApp user = findUserById(userId);
        Permission permission = permissionRepository.findByName(permissionName)
                .orElseThrow(() -> new ResourceNotFoundException("Permission non trouvée: " + permissionName));

        String currentAdmin = getCurrentUsername();

        // Check if user already has a custom permission for this
        Optional<UserPermission> existingPermission = userPermissionRepository
                .findByUserIdAndPermissionId(userId, permission.getId());

        if (existingPermission.isPresent()) {
            // Update existing custom permission
            UserPermission userPermission = existingPermission.get();
            userPermission.setGranted(granted);
            userPermission.setAssignedBy(currentAdmin);
            userPermissionRepository.save(userPermission);
        } else {
            // Create new custom permission
            UserPermission newUserPermission = UserPermission.builder()
                    .user(user)
                    .permission(permission)
                    .granted(granted)
                    .assignedBy(currentAdmin)
                    .build();
            userPermissionRepository.save(newUserPermission);
            user.getUserPermissions().add(newUserPermission);
        }

        log.info("Permission {} updated for user {} successfully", permissionName, userId);
        return userMapper.toDTO(userRepository.findById(userId).orElseThrow());
    }

    @Override
    public UserResponseDTO removeUserCustomPermission(Long userId, String permissionName) {
        log.info("Removing custom permission {} from user {}", permissionName, userId);

        UserApp user = findUserById(userId);
        Permission permission = permissionRepository.findByName(permissionName)
                .orElseThrow(() -> new ResourceNotFoundException("Permission non trouvée: " + permissionName));

        Optional<UserPermission> existingPermission = userPermissionRepository
                .findByUserIdAndPermissionId(userId, permission.getId());

        if (existingPermission.isPresent()) {
            user.getUserPermissions().remove(existingPermission.get());
            userPermissionRepository.delete(existingPermission.get());
            log.info("Custom permission {} removed from user {} successfully", permissionName, userId);
        }

        return userMapper.toDTO(userRepository.findById(userId).orElseThrow());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserPermissionDTO> getUserCustomPermissions(Long userId) {
        log.info("Fetching custom permissions for user {}", userId);
        findUserById(userId); // Validate user exists

        return userPermissionMapper.toDTOList(userPermissionRepository.findByUserId(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionDTO> getUserEffectivePermissions(Long userId) {
        log.info("Calculating effective permissions for user {}", userId);
        UserApp user = findUserById(userId);

        // Start with role permissions
        Set<Permission> effectivePermissions = new HashSet<>();
        if (user.getRole() != null) {
            effectivePermissions.addAll(user.getRole().getPermissions());
        }

        // Apply custom permissions
        for (UserPermission userPermission : user.getUserPermissions()) {
            if (userPermission.getGranted()) {
                effectivePermissions.add(userPermission.getPermission());
            } else {
                effectivePermissions.remove(userPermission.getPermission());
            }
        }

        List<Permission> sortedPermissions = effectivePermissions.stream()
                .sorted(Comparator.comparing(Permission::getCategory)
                        .thenComparing(Permission::getName))
                .collect(Collectors.toList());

        return permissionMapper.toDTOList(sortedPermissions);
    }


    private UserApp findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + userId));
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "SYSTEM";
    }
}

