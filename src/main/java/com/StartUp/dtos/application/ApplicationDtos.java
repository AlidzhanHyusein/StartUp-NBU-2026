package com.StartUp.dtos.application;

import com.StartUp.enums.ApplicationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data

public class ApplicationDtos {
    public record ApplicationResponse(
            Long id,
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

    public record ApplicationRequest(
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            String city,
            String resumeUrl,
            String messageToCompany,
            ApplicationStatus status
    ) {
    }

    public record JobSummary(
            Long id,
            Long studentId,
            String title,
            String company,
            String description
    ) {}
}