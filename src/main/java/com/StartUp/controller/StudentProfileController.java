package com.StartUp.controller;

import com.StartUp.dtos.student.StudentDtos;
import com.StartUp.service.StudentProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Tag(name = "Student Profile", description = "Manage student profiles and CV uploads")
public class StudentProfileController {

    private final StudentProfileService studentProfileService;

    @Operation(summary = "Get my student profile", description = "Returns the authenticated student's profile information")
    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentDtos.StudentProfileResponse> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(studentProfileService.getMyProfile(userDetails.getUsername()));
    }

    @Operation(summary = "Update my student profile", description = "Updates the authenticated student's profile details such as bio, skills and education")
    @PutMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentDtos.StudentProfileResponse> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody StudentDtos.UpdateStudentProfileRequest request) {
        return ResponseEntity.ok(studentProfileService.updateProfile(userDetails.getUsername(), request));
    }

    @Operation(summary = "Upload CV", description = "Uploads a CV file for the authenticated student")
    @PostMapping("/me/cv")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, String>> uploadCv(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        String url = studentProfileService.uploadCv(userDetails.getUsername(), file);
        return ResponseEntity.ok(Map.of("cvUrl", url));
    }

    @Operation(summary = "Get student profile by user ID", description = "Returns a public student profile by their user ID — accessible to all authenticated users")
    @GetMapping("/{userId}")
    public ResponseEntity<StudentDtos.StudentProfileResponse> getStudentProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(studentProfileService.getProfileByUserId(userId));
    }
}