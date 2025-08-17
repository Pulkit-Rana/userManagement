package com.syncNest.user_management.service;

import com.syncNest.user_management.domain.User;
import com.syncNest.user_management.domain.UserRole;
import com.syncNest.user_management.modal.UserCreateDTO;
import com.syncNest.user_management.modal.UserDTO;
import com.syncNest.user_management.modal.UserProfileDTO;
import com.syncNest.user_management.repository.RoleRepository;
import com.syncNest.user_management.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManagementService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(UserCreateDTO userDTO, User requester) {
        validateRolePermissions(userDTO.getRoles(), requester);
        Set<UserRole> roles = userDTO.getRoles().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid role: " + roleName)))
                .collect(Collectors.toSet());

        User newUser = User.builder()
                .username(userDTO.getUsername())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .roles(roles)
                .enabled(true)
                .build();

        return userRepository.save(newUser);
    }

    public List<User> getAllUsers() {
        // Added filter to exclude deleted users and improve efficiency
        return userRepository.findAll()
                .stream()
                .filter(user -> !user.isDeleted())
                .toList();
    }

    private void validateRolePermissions(Set<String> requestedRoles, User requester) {
        if (requester.isAdmin()) return;
        // Moderator can only create users with role 'USERS'
        boolean containsInvalidRole = requestedRoles.stream()
                .anyMatch(role -> !role.equalsIgnoreCase("ROLE_USER"));

        if (containsInvalidRole || requestedRoles.isEmpty()) {
            throw new AccessDeniedException("Moderators can only assign USER role");
        }
    }
//    // Add missing method
//    private UserRole getDefaultRole() {
//        return roleRepository.findByName("ROLE_USER")
//                .orElseThrow(() -> new IllegalStateException("Default role not found"));
//    }

    @Transactional
    public User updateUser(UUID userId, UserCreateDTO userDTO, User requester) {
        validateRolePermissions(userDTO.getRoles(), requester);
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        // Validate username uniqueness if changing
        if (!existingUser.getUsername().equalsIgnoreCase(userDTO.getUsername())) {
            if (userRepository.existsByUsername(userDTO.getUsername())) {
                throw new IllegalArgumentException("Username already exists");
            }
        }
        // Update fields
        existingUser.setUsername(userDTO.getUsername());
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        // Update roles
        Set<UserRole> updatedRoles = userDTO.getRoles().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid role: " + roleName)))
                .collect(Collectors.toSet());
        existingUser.setRoles(updatedRoles);

        return userRepository.save(existingUser);
    }

    @Transactional
    public User toggleUserStatus(UUID userId, boolean enable, User requester) {
        if (!requester.isAdmin()) {
            throw new AccessDeniedException("Only admins can modify user status");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setEnabled(enable);
        return userRepository.save(user);
    }

    @Transactional
    public void softDeleteUser(UUID userId, UUID requesterId) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new EntityNotFoundException("Requester not found"));

        if (!requester.isAdmin()) {
            throw new AccessDeniedException("Only admins can delete users");
        }

        if (requesterId.equals(userId)) {
            throw new AccessDeniedException("You cannot delete yourself!");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setDeleted(true);
        user.setEnabled(false); // Ensure user cannot log in
        userRepository.save(user);
    }

    public User findUserByEmail(String email) {
        return userRepository.findByUsername(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    public UserDTO getCurrentUserProfile(User currentUser) {
        return UserDTO.builder()
                .id(currentUser.getId())
                .roles(currentUser.getRoles().stream()
                        .map(UserRole::getName)
                        .collect(Collectors.toSet()))
                .profile(UserProfileDTO.builder()
                        .firstName(currentUser.getProfile().getFirstName())
                        .lastName(currentUser.getProfile().getLastName())
                        .profilePictureUrl(currentUser.getProfile().getProfilePictureUrl())
                        .build())
                .build();
    }

}
