package com.veersa.usermanagement.controller;

import com.veersa.usermanagement.domain.User;
import com.veersa.usermanagement.modal.UserCreateDTO;
import com.veersa.usermanagement.service.UserManagementService;
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
@RequestMapping("users")
@RequiredArgsConstructor
@Tag(name = "Users")
public class UserController {

    private final UserManagementService userService;

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
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
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
    @PreAuthorize("hasRole('ADMIN','MODERATOR')")
    public ResponseEntity<User> toggleUserStatus(
            @PathVariable UUID userId,
            @RequestParam boolean enable,
            @AuthenticationPrincipal User requester
    ) {
        return ResponseEntity.ok(userService.toggleUserStatus(userId, enable, requester));
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