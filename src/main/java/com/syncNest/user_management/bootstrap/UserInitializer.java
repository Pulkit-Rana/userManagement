package com.syncNest.user_management.bootstrap;

import com.syncNest.user_management.domain.AuthProvider;
import com.syncNest.user_management.domain.User;
import com.syncNest.user_management.domain.UserRole;
import com.syncNest.user_management.repository.RoleRepository;
import com.syncNest.user_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.init.admin.password}")
    private String adminPlainPassword;

    @Value("${app.init.manager.password}")
    private String managerPlainPassword;

    @Override
    @Transactional
    public void run(String... args) {
        Optional<UserRole> adminRole = roleRepository.findByName("ADMIN");
        Optional<UserRole> managerRole = roleRepository.findByName("MANAGER");

        if (adminRole.isEmpty() || managerRole.isEmpty()) {
            log.error("Required roles not found. Make sure roles are initialized first.");
            throw new IllegalStateException("Required roles not found. Roles must be initialized first.");
        }

        String adminUsername = "admin@example.com";
        if (!userRepository.existsByUsername(adminUsername)) {
            User adminUser = User.builder()
                    .username(adminUsername)
                    .password(ensureEncoded(adminPlainPassword)) // ✅ encode here
                    .roles(Set.of(adminRole.get()))
                    .enabled(true)
                    .isLocked(false)
                    .isVerified(true)
                    .provider(AuthProvider.LOCAL)
                    .build();
            userRepository.save(adminUser);
            log.info("Admin added successfully.");
        }

        String managerUsername = "manager@example.com";
        if (!userRepository.existsByUsername(managerUsername)) {
            User managerUser = User.builder()
                    .username(managerUsername)
                    .password(ensureEncoded(managerPlainPassword)) // ✅ encode here
                    .roles(Set.of(managerRole.get()))
                    .enabled(true)
                    .isLocked(false)
                    .isVerified(true)
                    .provider(AuthProvider.LOCAL)
                    .build();
            userRepository.save(managerUser);
            log.info("Manager added successfully.");
        } else {
            log.info("Users already exist");
        }
    }

    /**
     * Double-encode se bachne ke liye: agar already bcrypt hash hai to waise hi return kar do
     */
    private String ensureEncoded(String rawOrEncoded) {
        if (rawOrEncoded == null) throw new IllegalArgumentException("Password cannot be null");
        if (isBcrypt(rawOrEncoded)) return rawOrEncoded;
        return passwordEncoder.encode(rawOrEncoded);
    }

    /**
     * BCrypt hashes usually start with $2a$, $2b$, or $2y$
     */
    private boolean isBcrypt(String value) {
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }
}
