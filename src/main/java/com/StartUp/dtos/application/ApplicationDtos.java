package com.StartUp.dtos.application;

import com.StartUp.enums.ApplicationStatus;
import java.time.LocalDateTime;

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
            LocalDateTime appliedAt
    ) {
    }

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