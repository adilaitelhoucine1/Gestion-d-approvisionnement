package com.tricol.gestionstock.service.auth;

import com.tricol.gestionstock.dto.auth.*;
import com.tricol.gestionstock.entity.security.UserApp;
import com.tricol.gestionstock.repository.security.RoleAppRepository;
import com.tricol.gestionstock.repository.security.UserAppRepository;
import com.tricol.gestionstock.security.CustomUserDetails;
import com.tricol.gestionstock.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserAppRepository userAppRepository;

    @Autowired
    private RoleAppRepository roleAppRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthAuditService auditService;

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            String accessToken = jwtUtils.generateAccessToken(userDetails.getUsername());
            String refreshToken = jwtUtils.generateRefreshToken(userDetails.getUsername());

            UserApp user = userAppRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setLastLogin(LocalDateTime.now());
            userAppRepository.save(user);


            auditService.logLoginSuccess(userDetails.getUsername());

            UserInfoDTO userInfo = buildUserInfo(userDetails, user);

            return AuthResponseDTO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(jwtUtils.getJwtExpirationMs())
                    .user(userInfo)
                    .build();
        } catch (Exception e) {
            // Audit log: failed login
            auditService.logLoginFailure(loginRequest.getUsername(), e.getMessage());
            throw e;
        }
    }

    @Transactional
    public MessageResponseDTO register(RegisterRequestDTO registerRequest) {

        if (userAppRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Error: Username is already taken!");
        }


        if (userAppRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }


        UserApp user = UserApp.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

         userAppRepository.save(user);

        // Audit log: user registration
        auditService.logRegistration(registerRequest.getUsername());

        return MessageResponseDTO.builder()
                .message("User registered successfully! Please wait for an administrator to assign you a role.")
                .build();
    }

    @Transactional
    public TokenRefreshResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        String refreshToken = request.getRefreshToken();


        if (!jwtUtils.validateToken(refreshToken)) {
            throw new RuntimeException("Refresh token is invalid or expired!");
        }


        String username = jwtUtils.getUsernameFromToken(refreshToken);


        UserApp user = userAppRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found!"));


        String newAccessToken = jwtUtils.generateAccessToken(username);
        String newRefreshToken = jwtUtils.generateRefreshToken(username);

        // Audit log: token refresh
        auditService.logTokenRefresh(username);

        return TokenRefreshResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(jwtUtils.getJwtExpirationMs())
                .build();
    }

    public UserInfoDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found!");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UserApp user = userAppRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found!"));

        return buildUserInfo(userDetails, user);
    }

    private UserInfoDTO buildUserInfo(CustomUserDetails userDetails, UserApp user) {
        return UserInfoDTO.builder()
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .firstName(userDetails.getFirstName())
                .lastName(userDetails.getLastName())
                .roles(userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .filter(auth -> auth.startsWith("ROLE_"))
                        .map(auth -> auth.substring(5)) // Remove "ROLE_" prefix
                        .collect(Collectors.toSet()))
                .permissions(userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .filter(auth -> !auth.startsWith("ROLE_"))
                        .collect(Collectors.toSet()))
                .build();
    }
}

