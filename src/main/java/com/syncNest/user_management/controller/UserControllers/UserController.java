package com.syncNest.user_management.controller.UserControllers;

import com.syncNest.user_management.domain.User;
import com.syncNest.user_management.modal.UserCreateDTO;
import com.syncNest.user_management.modal.UserDTO;
import com.syncNest.user_management.service.UserManagementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("user")
@RequiredArgsConstructor
@Tag(name = "Users")
public class UserController {

    private final UserManagementService userService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> getCurrentUserProfile(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(userService.getCurrentUserProfile(currentUser));
    }


    @PostMapping("addUser")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<User> createUser(
            @Valid @RequestBody UserCreateDTO userDTO,
            @AuthenticationPrincipal User requester
    ) {
        User createdUser = userService.createUser(userDTO, requester);
        URI location = URI.create("/users/" + createdUser.getId());
        return ResponseEntity.created(location).body(createdUser);
    }

    @GetMapping("/allUsers")
    public ResponseEntity<List<User>> getAllActiveUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<User> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UserCreateDTO userDTO,
            @AuthenticationPrincipal User requester
    ) {
        return ResponseEntity.ok(userService.updateUser(userId, userDTO, requester));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> toggleUserStatus(
            @PathVariable UUID id,
            @RequestParam boolean enable,
            @AuthenticationPrincipal User requester
    ) {
        return ResponseEntity.ok(userService.toggleUserStatus(id, enable, requester));
    }

    @DeleteMapping("/delete/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @RequestParam UUID id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User requester = userService.findUserByEmail(userDetails.getUsername());
        userService.softDeleteUser(id, requester.getId());
        return ResponseEntity.noContent().build();
    }

}