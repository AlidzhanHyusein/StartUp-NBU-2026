package com.StartUp.controller;

import com.StartUp.dtos.employer.EmployerDtos;
import com.StartUp.service.EmployerProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/employers")
@RequiredArgsConstructor
public class EmployerProfileController {
    private final EmployerProfileService employerProfileService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<EmployerDtos.EmployerProfileResponse> getMyProfile(@AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(employerProfileService.getMyProfile(userDetails.getUsername()));
    }


    @PutMapping("/me")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<EmployerDtos.EmployerProfileResponse> updateMyProfile(@AuthenticationPrincipal UserDetails userDetails, @RequestBody EmployerDtos.UpdateEmployerProfileRequest request){
        return ResponseEntity.ok(employerProfileService.updateProfile(userDetails.getUsername(),request));
    }

    @PostMapping("/me/logo")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<Map<String,String>> uploadLogo(@AuthenticationPrincipal UserDetails userDetails, @RequestParam("file") MultipartFile file){
        String url = employerProfileService.uploadLogo(userDetails.getUsername(),file);
        return ResponseEntity.ok(Map.of("logoUrl",url));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<EmployerDtos.EmployerProfileResponse> getEmployerProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(employerProfileService.getProfileByUserId(userId));
    }
}
