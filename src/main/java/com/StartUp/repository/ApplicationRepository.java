package com.StartUp.repository;

import com.StartUp.entity.Application;
import com.StartUp.entity.Job;
import com.StartUp.entity.StudentProfile;
import com.StartUp.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application,Long> {

    Page<Application> findAllByStatus(ApplicationStatus status, Pageable pageable);

    boolean existsByStudentAndJob(StudentProfile studentProfile, Job job);
}
