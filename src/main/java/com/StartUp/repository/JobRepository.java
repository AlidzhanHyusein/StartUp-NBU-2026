package com.StartUp.repository;

import com.StartUp.entity.EmployerProfile;
import com.StartUp.entity.Job;
import com.StartUp.enums.JobCategory;
import com.StartUp.enums.JobLocation;
import com.StartUp.enums.JobStatus;
import com.StartUp.enums.JobType;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor {
    Page<Job> findAllByEmployer(EmployerProfile employer, Pageable pageable);

    List<Job> findAllByCategory(JobCategory jobCategory);

    List<Job> findAllByType(JobType jobType);

    List<Job> findAllByLocation(JobLocation jobLocation);

    List<Job> findAllByStatus(JobStatus jobStatus);

    List<Job> findAllByEmployerAndStatus(EmployerProfile employer, JobStatus jobStatus);

    List<Job> findByEndDateLessThanEqual(LocalDate date);
}
