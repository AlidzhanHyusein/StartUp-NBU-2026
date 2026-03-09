package com.StartUp.service;

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

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final EmployerProfileRepository employerProfileRepository;


    public Page<Application> findAllByStatus(ApplicationStatus status, Pageable pageable){
        return applicationRepository.findAllByStatus(status,pageable);
    }


    @Transactional
    public Application appliedToJob(Long jobId) {

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
                .build();

        studentProfile.getApplication().add(newApplication);
        job.getApplication().add(newApplication);

        return applicationRepository.save(newApplication);
    }

    @Transactional
    public Application applicationStatusChange(Long applicationId, ApplicationStatus status) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        EmployerProfile currentEmployer = employerProfileRepository.findByUser_Email(email)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Employer not found"));

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Application not found"));

        if (!application.getJob().getEmployer().getId().equals(currentEmployer.getId())) {
            throw new AppExceptions.BadRequestException("This job offer does not belong to you");
        }

        application.setStatus(status);
        return applicationRepository.save(application);
    }

    private Job findJobById(Long jobId){
        return jobRepository.findById(jobId).orElseThrow(() -> new AppExceptions.ResourceNotFoundException("The job was not found"));
    }


}
