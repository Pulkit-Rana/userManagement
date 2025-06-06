package com.veersa.usermanagement.bootstrap;

import com.veersa.usermanagement.domain.UserRole;
import com.veersa.usermanagement.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class RoleInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Transactional
    public void run(String... args) {
        List<String> roles = List.of("USER", "ADMIN", "MANAGER");
        List<UserRole> existingRoles = roleRepository.findAllByNameIn(roles);

        Set<String> existingRoleNames = existingRoles.stream()
                .map(UserRole::getName)
                .collect(Collectors.toSet());

        List<UserRole> rolesToSave = roles.stream()
                .filter(role -> !existingRoleNames.contains(role))
                .map(roleName -> new UserRole(null, roleName))
                .collect(Collectors.toList());

        if (!rolesToSave.isEmpty()) {
            roleRepository.saveAll(rolesToSave);
            log.info("Roles '{}' added successfully.", roles);
        } else {
            log.info("Roles already exists.");
        }

    }
}