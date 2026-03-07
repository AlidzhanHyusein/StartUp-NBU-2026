package com.StartUp.service;

import com.StartUp.dtos.job.JobDtos;
import com.StartUp.entity.EmployerProfile;
import com.StartUp.entity.Job;
import com.StartUp.enums.JobStatus;
import com.StartUp.exception.AppExceptions;
import com.StartUp.repository.EmployerProfileRepository;
import com.StartUp.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobService {
    private final JobRepository jobRepository;
    private final EmployerProfileRepository employerProfileRepository;

    public JobService(JobRepository jobRepository, EmployerProfileRepository employerProfileRepository) {
        this.jobRepository = jobRepository;
        this.employerProfileRepository = employerProfileRepository;
    }

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

    public List<JobDtos.JobResponse> getAllMyJobs(String username) {
        EmployerProfile employer = employerProfileRepository.findByUserEmail(username)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Employer not found"));

        List<Job> jobs = jobRepository.findAllByEmployer(employer);
        return jobs.stream().map(this::mapToJobResponse).toList();
    }

    public JobDtos.JobResponse getMyJob(Long jobId, String username) {
        EmployerProfile employer = employerProfileRepository.findByUserEmail(username)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Employer not found"));

        Job job = jobRepository.findById(jobId)
                .filter(j -> j.getEmployer().getId().equals(employer.getId()))
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Job not found"));

        return mapToJobResponse(job);
    }

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
        return new JobDtos.JobResponse(
                job.getId(),
                job.getEmployer(),
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