package com.StartUp.service;

import com.StartUp.dtos.job.JobDtos;
import com.StartUp.entity.EmployerProfile;
import com.StartUp.entity.Job;
import com.StartUp.enums.JobStatus;
import com.StartUp.exception.AppExceptions;
import com.StartUp.repository.EmployerProfileRepository;
import com.StartUp.repository.JobRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.print.Pageable;
import java.util.List;

@Service
public class JobService {
    private final JobRepository jobRepository;
    private final EmployerProfileRepository employerProfileRepository;

    public JobService(JobRepository jobRepository, EmployerProfileRepository employerProfileRepository) {
        this.jobRepository = jobRepository;
        this.employerProfileRepository = employerProfileRepository;
    }

    @Transactional
    public JobDtos.JobResponse addMyJob(JobDtos.JobRequest jobRequest, String username) {
        EmployerProfile employer = employerProfileRepository.findByUserEmail(username)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Employer not found"));

        Job job = new Job();
        job.setEmployer(employer);
        job.setTitle(jobRequest.title());
        job.setCategory(jobRequest.category());
        job.setType(jobRequest.type());
        job.setDuration(jobRequest.duration());
        job.setSalary(jobRequest.salary());
        job.setLocation(jobRequest.location());
        job.setDescription(jobRequest.description());
        job.setStartDate(jobRequest.startDate());
        job.setEndDate(jobRequest.endDate());
        job.setStatus(JobStatus.OPEN);

        return mapToJobResponse(jobRepository.save(job));
    }

    @Transactional
    public Page<JobDtos.JobResponse> getAllMyJobs(String username, Pageable pageable) {
        EmployerProfile employer = employerProfileRepository.findByUserEmail(username)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Employer not found"));

        Page<Job> jobs = jobRepository.findAllByEmployer(employer, pageable);
        return jobs.map(this::mapToJobResponse);
    }

    @Transactional
    public JobDtos.JobResponse getMyJob(Long jobId, String username) {
        EmployerProfile employer = employerProfileRepository.findByUserEmail(username)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Employer not found"));

        Job job = jobRepository.findById(jobId)
                .filter(j -> j.getEmployer().getId().equals(employer.getId()))
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Job not found"));

        return mapToJobResponse(job);
    }

    @Transactional
    public JobDtos.JobResponse updateMyJob(Long jobId, JobDtos.JobRequest jobRequest, String username) {
        EmployerProfile employer = employerProfileRepository.findByUserEmail(username)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Employer not found"));

        Job job = jobRepository.findById(jobId)
                .filter(j -> j.getEmployer().getId().equals(employer.getId()))
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Job not found"));

        job.setTitle(jobRequest.title());
        job.setCategory(jobRequest.category());
        job.setType(jobRequest.type());
        job.setDuration(jobRequest.duration());
        job.setSalary(jobRequest.salary());
        job.setLocation(jobRequest.location());
        job.setDescription(jobRequest.description());
        job.setStartDate(jobRequest.startDate());
        job.setEndDate(jobRequest.endDate());

        return mapToJobResponse(jobRepository.save(job));
    }

    @Transactional
    public void deleteMyJob(Long jobId, String username) {
        EmployerProfile employer = employerProfileRepository.findByUserEmail(username)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Employer not found"));

        Job job = jobRepository.findById(jobId)
                .filter(j -> j.getEmployer().getId().equals(employer.getId()))
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Job not found"));

        job.setStatus(JobStatus.CLOSED);
        jobRepository.save(job);
    }

    private JobDtos.JobResponse mapToJobResponse(Job job) {
        EmployerProfile ep = job.getEmployer();
        JobDtos.EmployerSummary employerSummary = new JobDtos.EmployerSummary(ep.getUser().getFirstName(), ep.getUser().getLastName(), ep.getCompanyName(), ep.getUser().getEmail());
        return new JobDtos.JobResponse(
                job.getId(),
                employerSummary,
                job.getTitle(),
                job.getCategory(),
                job.getType(),
                job.getDuration(),
                job.getSalary(),
                job.getLocation(),
                job.getDescription(),
                job.getStartDate(),
                job.getEndDate()
        );
    }
}