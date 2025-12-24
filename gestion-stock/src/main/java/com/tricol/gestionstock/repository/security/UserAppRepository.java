package com.tricol.gestionstock.repository.security;

import com.tricol.gestionstock.entity.security.UserApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAppRepository extends JpaRepository<UserApp, Long> {

    Optional<UserApp> findByUsername(String username);

    Optional<UserApp> findByEmail(String email);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);
}

