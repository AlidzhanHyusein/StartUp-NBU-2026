package com.StartUp.controller;

import com.StartUp.dtos.user.UserDtos;
import com.StartUp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "Manage user profiles and avatars")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get my profile", description = "Returns the authenticated user's profile information")
    @GetMapping("/me")
    public ResponseEntity<UserDtos.UserProfileResponse> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getProfile(userDetails.getUsername()));
    }

    @Operation(summary = "Update my profile", description = "Updates the authenticated user's profile details such as first name and last name")
    @PutMapping("/me")
    public ResponseEntity<UserDtos.UserProfileResponse> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserDtos.UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(userDetails.getUsername(), request));
    }

    @Operation(summary = "Upload avatar", description = "Uploads a profile picture for the authenticated user")
    @PostMapping("/me/avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        String url = userService.uploadAvatar(userDetails.getUsername(), file);
        return ResponseEntity.ok(Map.of("avatarUrl", url));
    }

    @Operation(summary = "Get user by ID", description = "Returns a user's profile by their ID — accessible to all authenticated users")
    @GetMapping("/{id}")
    public ResponseEntity<UserDtos.UserProfileResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getProfileById(id));
    }
}
