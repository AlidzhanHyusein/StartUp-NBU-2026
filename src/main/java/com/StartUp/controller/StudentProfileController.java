package com.StartUp.controller;

import com.StartUp.dtos.student.StudentDtos;
import com.StartUp.service.StudentProfileService;
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
public class StudentProfileController {

    private final StudentProfileService studentProfileService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentDtos.StudentProfileResponse> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(studentProfileService.getMyProfile(userDetails.getUsername()));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentDtos.StudentProfileResponse> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody StudentDtos.UpdateStudentProfileRequest request) {
        return ResponseEntity.ok(studentProfileService.updateProfile(userDetails.getUsername(), request));
    }

    @PostMapping("/me/cv")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, String>> uploadCv(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        String url = studentProfileService.uploadCv(userDetails.getUsername(), file);
        return ResponseEntity.ok(Map.of("cvUrl", url));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<StudentDtos.StudentProfileResponse> getStudentProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(studentProfileService.getProfileByUserId(userId));
    }
}
