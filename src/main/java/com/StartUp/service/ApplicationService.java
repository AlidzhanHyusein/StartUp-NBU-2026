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


@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository  applicationRepository;
    private final JobRepository          jobRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final MessageService         messageService;
    private final EmailService           emailService;

    private String currentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }


    @Transactional(readOnly = true)
    public Page<ApplicationDtos.ApplicationResponse> findAllByStatus(
            ApplicationStatus status, Pageable pageable) {
        String email = currentEmail();
        StudentProfile student = studentProfileRepository.findByUser_Email(email)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Student profile not found"));

        if (status != null) {
            return applicationRepository
                    .findAllByStudent_IdAndStatus(student.getId(), status, pageable)
                    .map(this::mapToResponse);
        }
        return applicationRepository
                .findAllByStudent_Id(student.getId(), pageable)
                .map(this::mapToResponse);
    }


    @Transactional
    public ApplicationDtos.ApplicationResponse appliedToJob(
            Long jobId, ApplicationDtos.ApplicationRequest request) {
        String email = currentEmail();
        StudentProfile student = studentProfileRepository.findByUser_Email(email)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Student profile not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Job not found"));

        if (applicationRepository.existsByStudentAndJob(student, job)) {
            throw new AppExceptions.BadRequestException("Already applied to this job");
        }

        String firstName = nvl(student.getUser().getFirstName(), nvl(request.firstName(), ""));
        String lastName  = nvl(student.getUser().getLastName(),
                nvl(request.lastName(),  ""));
        String userEmail = nvl(student.getUser().getEmail(),
                nvl(request.email(),     ""));
        String phone     = nvl(student.getPhone(),
                nvl(request.phoneNumber(), ""));
        String city      = nvl(student.getCity(),
                nvl(request.city(),       ""));

        Application application = Application.builder()
                .student(student)
                .job(job)
                .firstName(firstName)
                .lastName(lastName)
                .email(userEmail)
                .phoneNumber(phone)
                .city(city)
                .resumeUrl(request.resumeUrl())
                .messageToCompany(request.messageToCompany())
                .status(ApplicationStatus.PENDING)
                .build();

        Application saved = applicationRepository.save(application);
        return mapToResponse(
                applicationRepository.findByIdWithDetails(saved.getId()).orElse(saved)
        );
    }

    private String nvl(String value, String fallback) {
        return (value != null && !value.isBlank()) ? value : fallback;
    }

    public ApplicationDtos.ApplicationResponse applicationStatusChange(Long id, ApplicationStatus status) {
        return updateStatus(id, status);
    }

    public ApplicationDtos.ApplicationResponse withdrawApplication(Long applicationId) {
        return mapToResponse(withdraw(applicationId));
    }

    public Page<ApplicationDtos.ApplicationResponse> findAllEmployerApplications(
            ApplicationStatus status, Pageable pageable) {
        return findAllForEmployer(status, pageable);
    }

    @Transactional
    public Application withdraw(Long applicationId) {
        String email = currentEmail();
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Application not found"));
        StudentProfile student = studentProfileRepository.findByUser_Email(email)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Student profile not found"));
        if (!application.getStudent().getId().equals(student.getId())) {
            throw new AppExceptions.UnauthorizedException("You do not own this application");
        }
        application.setStatus(ApplicationStatus.WITHDRAWN);
        return applicationRepository.save(application);
    }


    @Transactional(readOnly = true)
    public Page<ApplicationDtos.ApplicationResponse> findAllForEmployer(
            ApplicationStatus status, Pageable pageable) {
        String email = currentEmail();
        EmployerProfile employer = employerProfileRepository.findByUser_Email(email)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Employer profile not found"));

        if (status != null) {
            return applicationRepository
                    .findAllByJob_Employer_IdAndStatus(employer.getId(), status, pageable)
                    .map(this::mapToResponse);
        }
        return applicationRepository
                .findAllByJob_Employer_Id(employer.getId(), pageable)
                .map(this::mapToResponse);
    }


    @Transactional
    public ApplicationDtos.ApplicationResponse updateStatus(Long applicationId, ApplicationStatus status) {
        String email = currentEmail();
        EmployerProfile employer = employerProfileRepository.findByUser_Email(email)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Employer profile not found"));
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Application not found"));
        if (!application.getJob().getEmployer().getId().equals(employer.getId())) {
            throw new AppExceptions.UnauthorizedException("You do not own this application");
        }
        application.setStatus(status);
        return mapToResponse(applicationRepository.save(application));
    }


    private ApplicationDtos.ApplicationResponse mapToResponse(Application application) {
        return new ApplicationDtos.ApplicationResponse(
                application.getId(),
                application.getStudent().getId(),
                application.getFirstName(),
                application.getLastName(),
                application.getEmail(),
                application.getPhoneNumber(),
                application.getCity(),
                application.getResumeUrl(),
                application.getMessageToCompany(),
                application.getStatus(),
                application.getAppliedAt(),
                new ApplicationDtos.JobSummary(
                        application.getJob().getId(),
                        application.getStudent().getId(),
                        application.getJob().getTitle(),
                        application.getJob().getEmployer() != null
                                ? application.getJob().getEmployer().getCompanyName() : "",
                        application.getJob().getDescription()
                )
        );
    }
}
