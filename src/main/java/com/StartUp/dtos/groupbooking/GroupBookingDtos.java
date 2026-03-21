package com.StartUp.dtos.groupbooking;

import com.StartUp.enums.GroupBookingStatus;
import com.StartUp.enums.GroupInviteStatus;

import java.time.LocalDateTime;
import java.util.List;

public class GroupBookingDtos {

    public record CreateRequest(
            Long jobId,
            List<Long> memberProfileIds,
            String message
    ) {}

    public record GroupBookingResponse(
            Long id,
            JobInfo job,
            StudentInfo leader,
            List<StudentInfo> members,
            List<InviteInfo> invites,
            int requiredSize,
            int currentSize,
            boolean allAccepted,
            GroupBookingStatus status,
            String message,
            LocalDateTime createdAt
    ) {}

    public record InviteResponse(
            Long inviteId,
            Long bookingId,
            JobInfo job,
            StudentInfo leader,
            String leaderMessage,
            GroupInviteStatus status,
            LocalDateTime createdAt
    ) {}

    public record InviteInfo(
            Long inviteId,
            StudentInfo student,
            GroupInviteStatus status,
            LocalDateTime respondedAt
    ) {}

    public record StatusUpdateRequest(GroupBookingStatus status) {}

    public record JobInfo(
            Long id,
            String title,
            String companyName,
            String category,
            String location,
            int requiredSize
    ) {}

    public record StudentInfo(
            Long profileId,
            Long userId,
            String firstName,
            String lastName,
            String university,
            String city
    ) {}
}
