package com.StartUp.service;

import com.StartUp.dtos.student.StudentDtos;
import com.StartUp.entity.Application;
import com.StartUp.entity.Job;
import com.StartUp.entity.StudentProfile;
import com.StartUp.entity.User;
import com.StartUp.enums.ApplicationStatus;
import com.StartUp.enums.Role;
import com.StartUp.exception.AppExceptions;
import com.StartUp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final StudentProfileService studentProfileService;
    private final UserService userService;
    private final UserRepository userRepository;

    public Page<List<Application>> findAllByStatus(ApplicationStatus status, Pageable page){

    }

    @Transactional
    public Application appliedToJob(Application application){

        Job job = findJobById(application.getJob().getId());


        StudentProfile studentProfile = studentProfileRepository.existsByUser_Email(application.getStudent().getUser().getEmail());


        Application application1 = Application.builder()
                .email(studentProfile.getUser().getEmail())
                .city(studentProfile.getCity())
                .firstName(studentProfile.getUser().getFirstName())
                .lastName(studentProfile.getUser().getLastName())
                .job(job)
                .student(studentProfile)
                .status(ApplicationStatus.PENDING)
                .build();

        job.setApplication((List<Application>) application1);
        return applicationRepository.save(application1);
    }


    private Job findJobById(Long jobId){
        return jobRepository.findById(jobId).orElseThrow(() -> new AppExceptions.ResourceNotFoundException("The job was not found"));
    }


}
