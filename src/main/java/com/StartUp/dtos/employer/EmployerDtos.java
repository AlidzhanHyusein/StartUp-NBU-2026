package com.StartUp.dtos.employer;

import java.time.LocalDateTime;

public class EmployerDtos {

    public record EmployerProfileResponse (
            Long id,
            Long userId,
            String firstName,
            String lastName,
            String email,
            String companyName,
            String description,
            String website,
            String phone,
            String city,
            String country,
            String companySize,
            String industry,
            String logoUrl,
            Boolean isVerified,
            LocalDateTime createdAt
    ){
    }

    public record UpdateEmployerProfileRequest(
            String companyName,
            String description,
            String website,
            String phone,
            String city,
            String country,
            String companySize,
            String industry
    ) {}
}
