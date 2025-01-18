package com.veersa.usermanagement.util;

import io.micrometer.common.lang.NonNull;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    private static final String SYSTEM_AUDITOR = "system";

    @NonNull
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (isUnauthenticatedOrAnonymous(authentication)) {
            // Log and return system auditor for unauthenticated requests
            logFallback();
            return Optional.of(SYSTEM_AUDITOR);
        }

        String username = authentication.getName();
        if (username == null || username.isBlank()) {
            // Handle unexpected null/empty username
            logFallback();
            return Optional.of(SYSTEM_AUDITOR);
        }

        logAuditor(username); // Log the active auditor
        return Optional.of(username);
    }

    // --- Helper Methods ---
    private boolean isUnauthenticatedOrAnonymous(Authentication authentication) {
        return authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken;
    }

    private void logAuditor(String auditor) {
        // Use a proper logging framework (e.g., SLF4J)
        System.out.printf("[AUDIT] Current auditor: %s%n", auditor);
    }

    private void logFallback() {
        System.out.printf("[AUDIT] Using fallback auditor: %s%n", AuditorAwareImpl.SYSTEM_AUDITOR);
    }
}