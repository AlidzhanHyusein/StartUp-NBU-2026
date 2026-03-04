package com.StartUp.dtos.student;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class StudentDtos {

    public record StudentProfileResponse(
            Long id,
            Long userId,
            String firstName,
            String lastName,
            String email,
            String avatarUrl,
            String bio,
            String university,
            String major,
            Integer graduationYear,
            LocalDate dateOfBirth,
            String phone,
            String city,
            String country,
            String cvUrl,
            String skills,
            String linkedinUrl,
            String githubUrl,
            LocalDateTime createdAt
    ) {}

    public record UpdateStudentProfileRequest(
            String bio,
            String university,
            String major,
            Integer graduationYear,
            LocalDate dateOfBirth,
            String phone,
            String city,
            String country,
            String skills,
            String linkedinUrl,
            String githubUrl
    ) {}
}
