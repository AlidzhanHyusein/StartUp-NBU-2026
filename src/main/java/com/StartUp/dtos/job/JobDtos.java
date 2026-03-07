package com.StartUp.dtos.job;

import com.StartUp.entity.EmployerProfile;
import com.StartUp.enums.JobCategory;
import com.StartUp.enums.JobLocation;
import com.StartUp.enums.JobType;

import java.time.LocalDate;

public class JobDtos {
    public record JobResponse (
            Long id,
            EmployerProfile employer,
            String title,
            JobCategory category,
            JobType type,
            Integer duration,
            Double salary,
            JobLocation location,
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
            Double salary,
            JobLocation location,
            String description,
            LocalDate startDate,
            LocalDate endDate
    ){
    }
}


