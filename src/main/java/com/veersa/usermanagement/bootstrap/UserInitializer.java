package com.veersa.usermanagement.bootstrap;

import com.veersa.usermanagement.domain.User;
import com.veersa.usermanagement.domain.UserRole;
import com.veersa.usermanagement.repository.RoleRepository;
import com.veersa.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class UserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // Retrieve both roles
        Optional<UserRole> adminRole = roleRepository.findByName("ADMIN");
        Optional<UserRole> managerRole = roleRepository.findByName("MANAGER");

        if (adminRole.isEmpty() || managerRole.isEmpty()) {
            log.error("Required roles not found. Make sure roles are initialized first.");
            throw new IllegalStateException("Required roles not found. Roles must be initialized first.");
        }

        // Create Admin User
        String adminUsername = "admin@example.com";
        if (!userRepository.existsByUsername(adminUsername)) {
            User adminUser = User.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode("adminPassword123"))
                    .roles(Set.of(adminRole.get()))
                    .enabled(true)
                    .isLocked(false)
                    .build();
            userRepository.save(adminUser);
            log.info("Admin user created successfully.");
        }
        // Create Manager User
        String managerUsername = "manager@example.com";
        if (!userRepository.existsByUsername(managerUsername)) {
            User managerUser = User.builder()
                    .username(managerUsername)
                    .password(passwordEncoder.encode("managerPassword123"))
                    .roles(Set.of(managerRole.get()))
                    .enabled(true)
                    .isLocked(false)
                    .build();
            userRepository.save(managerUser);
            log.info("Manager user created successfully.");
        } else {
            log.info("Users already exists");
        }
    }
}