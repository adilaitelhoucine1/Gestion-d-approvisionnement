package com.tricol.gestionstock.repository.security;

import com.tricol.gestionstock.entity.security.RoleApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleAppRepository extends JpaRepository<RoleApp, Long> {

    Optional<RoleApp> findByName(String name);

    Boolean existsByName(String name);
}

