package com.StartUp.controller;

import com.StartUp.dtos.employer.EmployerDtos;
import com.StartUp.entity.Application;
import com.StartUp.enums.ApplicationStatus;
import com.StartUp.service.ApplicationService;
import com.StartUp.service.EmployerProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
@Tag(name = "Employer Profile", description = "Manage employer profiles and company logos")
public class EmployerProfileController {

    private final EmployerProfileService employerProfileService;


    @Operation(summary = "Get my employer profile", description = "Returns the authenticated employer's profile information")
    @GetMapping("/me")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<EmployerDtos.EmployerProfileResponse> getMyProfile(@AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(employerProfileService.getMyProfile(userDetails.getUsername()));
    }

    @Operation(summary = "Update my employer profile", description = "Updates the authenticated employer's profile details such as company name and description")
    @PutMapping("/me")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<EmployerDtos.EmployerProfileResponse> updateMyProfile(@AuthenticationPrincipal UserDetails userDetails, @RequestBody EmployerDtos.UpdateEmployerProfileRequest request){
        return ResponseEntity.ok(employerProfileService.updateProfile(userDetails.getUsername(),request));
    }

    @Operation(summary = "Upload company logo", description = "Uploads a logo image for the authenticated employer's company")
    @PostMapping("/me/logo")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<Map<String,String>> uploadLogo(@AuthenticationPrincipal UserDetails userDetails, @RequestParam("file") MultipartFile file){
        String url = employerProfileService.uploadLogo(userDetails.getUsername(),file);
        return ResponseEntity.ok(Map.of("logoUrl",url));
    }

    @Operation(summary = "Get employer profile by user ID", description = "Returns a public employer profile by their user ID — accessible to all authenticated users")
    @GetMapping("/{userId}")
    public ResponseEntity<EmployerDtos.EmployerProfileResponse> getEmployerProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(employerProfileService.getProfileByUserId(userId));
    }

}