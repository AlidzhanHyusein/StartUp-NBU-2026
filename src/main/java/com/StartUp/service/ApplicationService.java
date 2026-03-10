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
        return applicationRepository.findAllByStatus(status,pageable).map(this::mapToResponse);
    }


    @Transactional()
    public ApplicationDtos.ApplicationResponse appliedToJob(Long jobId,String messageToCompany) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        StudentProfile studentProfile = studentProfileRepository.findByUser_Email(email)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Student profile not found"));

        Job job = findJobById(jobId);

        boolean alreadyApplied = applicationRepository.existsByStudentAndJob(studentProfile, job);
        if (alreadyApplied) {
            throw new IllegalStateException("Student has already applied to this job");
        }

        Application newApplication = Application.builder()
                .email(studentProfile.getUser().getEmail())
                .city(studentProfile.getCity())
                .firstName(studentProfile.getUser().getFirstName())
                .lastName(studentProfile.getUser().getLastName())
                .job(job)
                .student(studentProfile)
                .status(ApplicationStatus.PENDING)
                .resumeUrl(studentProfile.getCvUrl())
                .city(studentProfile.getCity())
                .phoneNumber(studentProfile.getPhone())
                .resumeUrl(studentProfile.getCvUrl())
                .messageToCompany(messageToCompany)
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

        application.setStatus(status);
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
                application.getJob().getTitle(),
                application.getJob().getEmployer().getCompanyName(),
                application.getJob().getDescription()
        );

        return new ApplicationDtos.ApplicationResponse(
                application.getId(),
                application.getStudent().getId(),
                application.getJob().getId(),
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
