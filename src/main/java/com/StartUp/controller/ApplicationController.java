package com.StartUp.controller;

import com.StartUp.dtos.application.ApplicationDtos;
import com.StartUp.enums.ApplicationStatus;
import com.StartUp.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Application", description = "CRUD operations about application")
@CrossOrigin
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Operation(summary = "Add application", description = "Adds a job application")
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApplicationDtos.ApplicationResponse> applyToJob(@RequestParam Long id, @RequestPart(required = false)@RequestBody  String messageToCompany) {
        ApplicationDtos.ApplicationResponse response = applicationService.appliedToJob(id,messageToCompany);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get all applications", description = "Gets all job applications")
    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Page<ApplicationDtos.ApplicationResponse>> getAllApplications(@RequestParam ApplicationStatus status,
                                                                                        @PageableDefault(size = 20) Pageable page) {
        return ResponseEntity.ok(applicationService.findAllByStatus(status, page));
    }

    @Operation(summary = "Change application status", description = "Changes the status of a job application")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ApplicationDtos.ApplicationResponse> changeApplicationStatus(@PathVariable Long id,
                                                                                       @RequestParam ApplicationStatus status) {
        ApplicationDtos.ApplicationResponse response = applicationService.applicationStatusChange(id, status);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get all applications for employer", description = "Employer sees all applications to their jobs")
    @GetMapping("/employer")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<Page<ApplicationDtos.ApplicationResponse>> getEmployerApplications(
            @RequestParam(required = false) ApplicationStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(applicationService.findAllEmployerApplications(status, pageable));
    }


}