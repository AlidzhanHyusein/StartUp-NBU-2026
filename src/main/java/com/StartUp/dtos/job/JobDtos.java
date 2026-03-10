package com.StartUp.dtos.job;

import com.StartUp.entity.EmployerProfile;
import com.StartUp.enums.JobCategory;
import com.StartUp.enums.JobLocation;
import com.StartUp.enums.JobStatus;
import com.StartUp.enums.JobType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class JobDtos {
    public record JobResponse (
            Long id,
            EmployerSummary employer,
            String title,
            JobCategory category,
            JobType type,
            Integer duration,
            BigDecimal salary,
            JobLocation location,
            JobStatus jobStatus,
            String description,
            LocalDate startDate,
            LocalDate endDate
    ){
    }

    public record JobRequest (
            String title,
            JobCategory category,
            JobType type,
            Integer duration,
            BigDecimal salary,
            JobLocation location,
            String description,
            LocalDate startDate,
            LocalDate endDate
    ){
    }

    public record EmployerSummary (
            String firstName,
            String lastName,
            String companyName,
            String email
    ){
    }

    public record JobFilter (
            JobCategory jobCategory,
            JobType jobType,
            JobLocation jobLocation,
            Integer duration,
            BigDecimal minSalary,
            JobStatus status
    ){
    }
}