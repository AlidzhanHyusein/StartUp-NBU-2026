package com.StartUp.controller;

import com.StartUp.dtos.job.JobDtos;
import com.StartUp.enums.JobCategory;
import com.StartUp.enums.JobLocation;
import com.StartUp.enums.JobStatus;
import com.StartUp.enums.JobType;
import com.StartUp.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;


@Tag(name = "Job", description = "CRUD operations about job")
@CrossOrigin
@RestController
@RequestMapping("/api/jobs")
public class JobController {
    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @Operation(summary = "Add job", description = "Adds a job offer")
    @PostMapping
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<JobDtos.JobResponse> addJob(@RequestBody JobDtos.JobRequest jobRequest,
                                                      @AuthenticationPrincipal UserDetails userDetails) {
        JobDtos.JobResponse response = jobService.addMyJob(jobRequest, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get all jobs", description = "Gets all job offers")
    @GetMapping
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<Page<JobDtos.JobResponse>> getAllJobs(@AuthenticationPrincipal UserDetails userDetails,
                                                                @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(jobService.getAllMyJobs(userDetails.getUsername(), pageable));

    }

    @Operation(summary = "Get job", description = "Gets job by id")
    @GetMapping("/{jobId}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<JobDtos.JobResponse> getJob(@PathVariable Long jobId,
                                                      @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(jobService.getMyJob(jobId, userDetails.getUsername()));

    }

    @Operation(summary = "Update job", description = "Updates job by id")
    @PutMapping("/{jobId}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<JobDtos.JobResponse> updateJob(@PathVariable Long jobId,
                                                         @RequestBody JobDtos.JobRequest jobRequest,
                                                         @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(jobService.updateMyJob(jobId, jobRequest, userDetails.getUsername()));

    }

    @Operation(summary = "Delete job", description = "Deletes job by id")
    @DeleteMapping("/{jobId}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<Void> deleteJob(@PathVariable Long jobId,
                                                         @AuthenticationPrincipal UserDetails userDetails) {
        jobService.deleteMyJob(jobId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('STUDENT', 'EMPLOYER')")
    public ResponseEntity<Page<JobDtos.JobResponse>> filterJobs(
            @RequestParam(name = "jobCategory", required = false) JobCategory jobCategory,
            @RequestParam(required = false) JobType jobType,
            @RequestParam(required = false) JobLocation jobLocation,
            @RequestParam(required = false) Integer duration,
            @RequestParam(required = false) BigDecimal minSalary,
            @RequestParam(required = false) JobStatus status,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {

        JobDtos.JobFilter jobFilter = new JobDtos.JobFilter(jobCategory, jobType, jobLocation, duration, minSalary, status);
        return ResponseEntity.ok(jobService.filterJobs(jobFilter, pageable));
    }
}
