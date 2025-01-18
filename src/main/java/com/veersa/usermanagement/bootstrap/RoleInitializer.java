package com.veersa.usermanagement.bootstrap;

import com.veersa.usermanagement.domain.UserRole;
import com.veersa.usermanagement.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class RoleInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Transactional
    public void run(String... args) {
        List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN", "ROLE_MANAGER");

        for (String roleName : roles) {
            try {
                roleRepository.findByName(roleName)
                        .orElseGet(() -> {
                            UserRole newRole = new UserRole();
                            newRole.setName(roleName);
                            return roleRepository.save(newRole);
                        });
                log.info("Role '{}' exists or has been added successfully.", roleName);
            } catch (DataAccessException e) {
                log.error("Database access error occurred while checking or saving role '{}'.", roleName, e);
            } catch (Exception e) {
                log.error("An unexpected error occurred while processing role '{}'.", roleName, e);
            }
        }
    }
}