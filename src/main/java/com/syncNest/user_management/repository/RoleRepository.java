package com.syncNest.user_management.repository;

import com.syncNest.user_management.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<UserRole, Long> {

    Optional<UserRole> findByName(String role);

    List<UserRole> findAllByNameIn(List<String> roles);
}
