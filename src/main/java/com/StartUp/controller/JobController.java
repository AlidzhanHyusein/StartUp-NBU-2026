package com.StartUp.controller;

import com.StartUp.dtos.job.JobDtos;
import com.StartUp.service.JobService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/jobs")
public class JobController {
    public final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<JobDtos.JobResponse> addJob(@RequestBody JobDtos.JobRequest jobRequest,
                                                      @AuthenticationPrincipal UserDetails userDetails) {
        JobDtos.JobResponse response = jobService.addMyJob(jobRequest, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<List<JobDtos.JobResponse>> getAllJobs(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(jobService.getAllMyJobs(userDetails.getUsername()));

    }

    @GetMapping("/{jobId}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<JobDtos.JobResponse> getJob(@PathVariable Long jobId,
                                                      @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(jobService.getMyJob(jobId, userDetails.getUsername()));

    }

    @PutMapping("/{jobId}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<JobDtos.JobResponse> updateJob(@PathVariable Long jobId,
                                                         @RequestBody JobDtos.JobRequest jobRequest,
                                                         @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(jobService.updateMyJob(jobId, jobRequest, userDetails.getUsername()));

    }

    @DeleteMapping("/{jobId}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<Void> deleteJob(@PathVariable Long jobId,
                                                         @AuthenticationPrincipal UserDetails userDetails) {
        jobService.deleteMyJob(jobId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
