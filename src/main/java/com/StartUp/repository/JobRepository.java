package com.StartUp.repository;

import com.StartUp.entity.EmployerProfile;
import com.StartUp.entity.Job;
import com.StartUp.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findAllByEmployer(EmployerProfile employer);

    List<Job> findAllByStatus(JobStatus jobStatus);

    List<Job> findAllByEmployerAndStatus(EmployerProfile employer, JobStatus jobStatus);
}
