package com.StartUp.dtos.application;

import com.StartUp.entity.Application;
import com.StartUp.entity.Job;
import com.StartUp.enums.ApplicationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data

public class ApplicationDtos {
    public record ApplicationResponse(
            Long id,
            Long studentId,
            Long jobId,
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            String city,
            String resumeUrl,
            String messageToCompany,
            ApplicationStatus status,
            LocalDateTime appliedAt,
            JobSummary job
    ) {
    }

    public record JobSummary(
            Long id,
            String title,
            String company,
            String description
    ) {}

    public record ApplicationRequest(
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            String city,
            String messageToCompany,
            ApplicationStatus status
    ) {
    }
}