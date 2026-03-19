package com.StartUp.service;

import com.StartUp.dtos.job.JobDtos;
import com.StartUp.entity.EmployerProfile;
import com.StartUp.entity.Job;
import com.StartUp.enums.JobStatus;
import com.StartUp.exception.AppExceptions;
import com.StartUp.repository.EmployerProfileRepository;
import com.StartUp.repository.JobRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDate;
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

        if(jobRequest.endDate().isBefore(jobRequest.startDate())){
            throw new AppExceptions.BadRequestException("End date must be greater than start date");
        }
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
        job.setGroupSize(jobRequest.groupSize());
        job.setStatus(JobStatus.OPEN);
        job.onCreate();

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

        if(jobRequest.endDate().isBefore(jobRequest.startDate())){
            throw new AppExceptions.BadRequestException("End date must be greater than start date");
        }

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

    @Transactional
    public Page<JobDtos.JobResponse> filterJobs(JobDtos.JobFilter jobFilter, Pageable pageable) {
        List<JobDtos.JobResponse> filteredJobs = jobRepository.findAll().stream()
                .filter(job -> jobFilter.jobCategory() == null || job.getCategory() == jobFilter.jobCategory())
                .filter(job -> jobFilter.jobType() == null || job.getType() == jobFilter.jobType())
                .filter(job -> jobFilter.jobLocation() == null || job.getLocation() == jobFilter.jobLocation())
                .filter(job -> jobFilter.duration() == null || job.getDuration() == jobFilter.duration())
                .filter(job -> jobFilter.minSalary() == null || job.getSalary().compareTo(jobFilter.minSalary()) >= 0)
                .filter(job -> jobFilter.status() == null || job.getStatus() == JobStatus.OPEN)
                .map(this::mapToJobResponse)
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filteredJobs.size());
        List<JobDtos.JobResponse> pageContent = start > filteredJobs.size() ? List.of() : filteredJobs.subList(start, end);

        return new PageImpl<>(pageContent, pageable, filteredJobs.size());
    }

    @Scheduled(cron = "0 59 23 * * *")
    @Transactional
    public void expireJobs() {
        jobRepository.expireJobs(LocalDate.now(), JobStatus.EXPIRED);
    }

    private JobDtos.JobResponse mapToJobResponse(Job job) {
        EmployerProfile ep = job.getEmployer();

        JobDtos.EmployerSummary employerSummary = new JobDtos.EmployerSummary(ep.getUser().getFirstName(), ep.getUser().getLastName(), ep.getCompanyName(), ep.getUser().getEmail());
        return new JobDtos.JobResponse(
                job.getId(),
                job.getEmployer().getUser().getId(),
                employerSummary,
                job.getTitle(),
                job.getGroupSize(),
                job.getCategory(),
                job.getType(),
                job.getDuration(),
                job.getSalary(),
                job.getLocation(),
                job.getStatus(),
                job.getDescription(),
                job.getStartDate(),
                job.getEndDate()
        );
    }
}