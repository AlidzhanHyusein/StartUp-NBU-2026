package com.StartUp.dtos.admin;

import com.StartUp.enums.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public class AdminDtos {

    public record DashboardStatsResponse(
            long totalUsers,
            long totalStudents,
            long totalEmployers,
            long pendingUsers,
            long blockedUsers,
            long totalCategories
    ) {}

    public record UpdateUserStatusRequest(
            @NotNull UserStatus status
    ) {}

    public record CategoryRequest(
            @NotBlank String name,
            String description
    ) {}
}
