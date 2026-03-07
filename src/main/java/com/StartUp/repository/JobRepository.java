package com.StartUp.repository;

import com.StartUp.entity.EmployerProfile;
import com.StartUp.entity.Job;
import com.StartUp.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;
@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    Page<Job> findAllByEmployer(EmployerProfile employer, Pageable pageable);

    List<Job> findAllByStatus(JobStatus jobStatus);

    List<Job> findAllByEmployerAndStatus(EmployerProfile employer, JobStatus jobStatus);
}
