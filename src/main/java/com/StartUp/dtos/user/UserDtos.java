package com.StartUp.dtos.user;

import com.StartUp.enums.Role;
import com.StartUp.enums.UserStatus;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class UserDtos {

    public record UserProfileResponse(
            Long id,
            String email,
            String firstName,
            String lastName,
            Role role,
            UserStatus status,
            String avatarUrl,
            LocalDateTime createdAt
    ) {}

    public record UpdateProfileRequest(
            @NotBlank String firstName,
            @NotBlank String lastName
    ) {}
}
