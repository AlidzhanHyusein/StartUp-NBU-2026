package com.StartUp.service;

import com.StartUp.dtos.application.ApplicationDtos;
import com.StartUp.entity.Application;
import com.StartUp.entity.EmployerProfile;
import com.StartUp.entity.Job;
import com.StartUp.entity.StudentProfile;
import com.StartUp.enums.ApplicationStatus;
import com.StartUp.exception.AppExceptions;
import com.StartUp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final EmployerProfileRepository employerProfileRepository;

    @Transactional(readOnly = true)
    public Page<ApplicationDtos.ApplicationResponse> findAllByStatus(ApplicationStatus status, Pageable pageable){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        StudentProfile student = studentProfileRepository.findByUser_Email(email)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Student profile not found"));

        if (status != null) {
            return applicationRepository.findAllByStudent_IdAndStatus(student.getId(), status, pageable)
                    .map(this::mapToResponse);
        }

        return applicationRepository.findAllByStudent_Id(student.getId(), pageable)
                .map(this::mapToResponse);
    }

    @Transactional()
    public ApplicationDtos.ApplicationResponse appliedToJob(Long jobId, ApplicationDtos.ApplicationRequest request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        StudentProfile studentProfile = studentProfileRepository.findByUser_Email(email)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Student profile not found"));

        Job job = findJobById(jobId);

        boolean alreadyApplied = applicationRepository.existsByStudentAndJob(studentProfile, job);
        if (alreadyApplied) {
            throw new AppExceptions.BadRequestException("Student has already applied to this job");
        }

        Application newApplication = Application.builder()
                .student(studentProfile)
                .job(job)
                .firstName(studentProfile.getUser().getFirstName())
                .lastName(studentProfile.getUser().getLastName())
                .email(studentProfile.getUser().getEmail())
                .phoneNumber(studentProfile.getPhone())
                .city(studentProfile.getCity())
                .resumeUrl(request.resumeUrl())
                .messageToCompany(request.messageToCompany())
                .status(ApplicationStatus.PENDING)
                .appliedAt(LocalDateTime.now())
                .build();

        studentProfile.getApplication().add(newApplication);
        job.getApplication().add(newApplication);

        return mapToResponse(applicationRepository.save(newApplication));
    }

    @Transactional
    public ApplicationDtos.ApplicationResponse applicationStatusChange(Long applicationId, ApplicationStatus status) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        EmployerProfile currentEmployer = employerProfileRepository.findByUser_Email(email)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Employer not found"));

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Application not found"));

        if (!application.getJob().getEmployer().getId().equals(currentEmployer.getId())) {
            throw new AppExceptions.BadRequestException("This job offer does not belong to you");
        }

        ApplicationStatus currentStatus = application.getStatus();

        if (currentStatus == ApplicationStatus.ACCEPTED) {
            if (status == ApplicationStatus.PENDING ||
                    status == ApplicationStatus.REVIEWED ||
                    status == ApplicationStatus.REJECTED) {
                throw new AppExceptions.BadRequestException(
                        "Cannot change status from ACCEPTED to " + status + ". Only COMPLETED is allowed."
                );
            }
        }

        if (currentStatus == ApplicationStatus.WITHDRAWN) {
            throw new AppExceptions.BadRequestException("Cannot change status of a withdrawn application.");
        }

        if (currentStatus == ApplicationStatus.REJECTED) {
            throw new AppExceptions.BadRequestException("Cannot change status of a rejected application.");
        }

        if(currentStatus == ApplicationStatus.COMPLETED) {
            throw new AppExceptions.BadRequestException("Cannot change status of a completed application.");
        }

        application.setStatus(status);
        return mapToResponse(applicationRepository.save(application));
    }

    @Transactional
    public ApplicationDtos.ApplicationResponse withdrawApplication(Long applicationId) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        StudentProfile student = studentProfileRepository.findByUser_Email(email)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Student profile not found"));

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Application not found"));

        if (!application.getStudent().getId().equals(student.getId())) {
            throw new AppExceptions.BadRequestException("This application does not belong to you");
        }

        if (application.getStatus() == ApplicationStatus.WITHDRAWN) {
            throw new AppExceptions.BadRequestException("Application is already withdrawn");
        }

        if (application.getStatus() == ApplicationStatus.COMPLETED) {
            throw new AppExceptions.BadRequestException(
                    "Cannot withdraw an application that is already " + application.getStatus()
            );
        }

        application.setStatus(ApplicationStatus.WITHDRAWN);
        return mapToResponse(applicationRepository.save(application));
    }

    @Transactional(readOnly = true)
    public Page<ApplicationDtos.ApplicationResponse> findAllEmployerApplications(ApplicationStatus status, Pageable pageable) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        EmployerProfile employer = employerProfileRepository.findByUser_Email(email)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Employer not found"));

        if (status != null) {
            return applicationRepository.findAllByJob_Employer_IdAndStatus(employer.getId(), status, pageable)
                    .map(this::mapToResponse);
        }

        return applicationRepository.findAllByJob_Employer_Id(employer.getId(), pageable)
                .map(this::mapToResponse);
    }

    private Job findJobById(Long jobId){
        return jobRepository.findById(jobId).orElseThrow(() -> new AppExceptions.ResourceNotFoundException("The job was not found"));
    }

    private ApplicationDtos.ApplicationResponse mapToResponse(Application application) {
        ApplicationDtos.JobSummary jobSummary = new ApplicationDtos.JobSummary(
                application.getJob().getId(),
                application.getStudent().getId(),
                application.getJob().getTitle(),
                application.getJob().getEmployer().getCompanyName(),
                application.getJob().getDescription()
        );

        return new ApplicationDtos.ApplicationResponse(
                application.getId(),
                application.getFirstName(),
                application.getLastName(),
                application.getEmail(),
                application.getPhoneNumber(),
                application.getCity(),
                application.getResumeUrl(),
                application.getMessageToCompany(),
                application.getStatus(),
                application.getAppliedAt(),
                jobSummary
        );
    }
}