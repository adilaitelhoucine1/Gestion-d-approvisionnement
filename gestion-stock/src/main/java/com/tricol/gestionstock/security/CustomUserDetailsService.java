package com.tricol.gestionstock.security;

import com.tricol.gestionstock.entity.security.UserApp;
import com.tricol.gestionstock.repository.security.UserAppRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserAppRepository userAppRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserApp user = userAppRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + username
                ));

        return CustomUserDetails.build(user);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        UserApp user = userAppRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with id: " + id
                ));

        return CustomUserDetails.build(user);
    }
}

