package com.StartUp.dtos.leaderboard;

import java.util.List;

public class LeaderboardDtos {

    public record LeaderboardResponse(
            String period,
            String month,
            List<LeaderboardEntry> entries,
            List<UniversityEntry> universities
    ) {}

    public record LeaderboardEntry(
            int rank,
            Long profileId,
            String firstName,
            String lastName,
            String university,
            String city,
            int completedShifts,
            double totalEarnings
    ) {}

    public record UniversityEntry(
            int rank,
            String university,
            int studentCount,
            int totalShifts,
            double totalEarnings
    ) {}
}
