package com.tricol.gestionstock.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tricol.gestionstock.entity.security.Permission;
import com.tricol.gestionstock.entity.security.UserApp;
import com.tricol.gestionstock.entity.security.UserPermission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public static CustomUserDetails build(UserApp user) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Add role if exists
        if (user.getRole() != null) {
            // Add role as authority (Spring Security expects "ROLE_" prefix)
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()));

            // Add default permissions from role
            Set<Permission> rolePermissions = user.getRole().getPermissions();
            rolePermissions.forEach(permission ->
                    authorities.add(new SimpleGrantedAuthority(permission.getName()))
            );
        }

        // Add or override with custom user permissions
        Set<UserPermission> userPermissions = user.getUserPermissions();
        userPermissions.forEach(userPermission -> {
            Permission permission = userPermission.getPermission();
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(permission.getName());

            if (userPermission.getGranted()) {
                // Grant permission (add if not exists)
                if (!authorities.contains(authority)) {
                    authorities.add(authority);
                }
            } else {
                // Revoke permission (remove if exists)
                authorities.remove(authority);
            }
        });

        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPassword(),
                authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

