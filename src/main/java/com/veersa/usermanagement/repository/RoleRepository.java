package com.veersa.usermanagement.repository;

import com.veersa.usermanagement.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<UserRole, Long> {

    Optional<UserRole> findByName(String role);

    List<UserRole> findAllByNameIn(List<String> roles);
}
