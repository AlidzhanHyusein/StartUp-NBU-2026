package com.StartUp.dtos.shiftswap;

import com.StartUp.enums.ShiftSwapStatus;

import java.time.LocalDateTime;

public class ShiftSwapDtos {

    public record PostSwapRequest(
            Long applicationId,
            String reason
    ) {}
    public record ShiftSwapResponse(
            Long id,
            JobInfo job,
            StudentInfo poster,
            StudentInfo claimer,
            ShiftSwapStatus status,
            String reason,
            LocalDateTime createdAt,
            LocalDateTime claimedAt,
            LocalDateTime resolvedAt
    ) {}

    public record JobInfo(
            Long id,
            String title,
            String companyName,
            String category,
            String type,
            String location,
            String startDate,
            String endDate,
            double salary
    ) {}

    public record StudentInfo(
            Long profileId,
            String firstName,
            String lastName,
            String university,
            String city
    ) {}
}
