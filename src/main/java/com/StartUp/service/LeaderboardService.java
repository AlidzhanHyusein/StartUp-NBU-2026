package com.StartUp.service;

import com.StartUp.dtos.leaderboard.LeaderboardDtos;
import com.StartUp.repository.LeaderboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final LeaderboardRepository leaderboardRepository;

    private static final int TOP_N = 50;


    @Transactional(readOnly = true)
    public LeaderboardDtos.LeaderboardResponse getMonthly(int year, int month) {
        LocalDateTime from = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime to   = from.plusMonths(1);

        String monthLabel = from.format(DateTimeFormatter.ofPattern("MMMM yyyy"));

        List<Object[]> studentRows    = leaderboardRepository.findTopStudentsInPeriod(from, to);
        List<Object[]> universityRows = leaderboardRepository.findUniversityStatsInPeriod(from, to);

        return new LeaderboardDtos.LeaderboardResponse(
                "MONTHLY",
                monthLabel,
                mapStudentRows(studentRows),
                mapUniversityRows(universityRows)
        );
    }


    @Transactional(readOnly = true)
    public LeaderboardDtos.LeaderboardResponse getAllTime() {
        List<Object[]> studentRows    = leaderboardRepository.findTopStudentsAllTime();
        List<Object[]> universityRows = leaderboardRepository.findUniversityStatsAllTime();

        return new LeaderboardDtos.LeaderboardResponse(
                "ALL_TIME",
                null,
                mapStudentRows(studentRows),
                mapUniversityRows(universityRows)
        );
    }


    private List<LeaderboardDtos.LeaderboardEntry> mapStudentRows(List<Object[]> rows) {
        List<LeaderboardDtos.LeaderboardEntry> result = new ArrayList<>();
        int rank = 1;
        for (Object[] row : rows) {
            if (rank > TOP_N) break;
            result.add(new LeaderboardDtos.LeaderboardEntry(
                    rank++,
                    ((Number) row[0]).longValue(),
                    (String)  row[1],
                    (String)  row[2],
                    (String)  row[3],
                    (String)  row[4],
                    ((Number) row[5]).intValue(),
                    ((Number) row[6]).doubleValue()
            ));
        }
        return result;
    }

    private List<LeaderboardDtos.UniversityEntry> mapUniversityRows(List<Object[]> rows) {
        List<LeaderboardDtos.UniversityEntry> result = new ArrayList<>();
        int rank = 1;
        for (Object[] row : rows) {
            result.add(new LeaderboardDtos.UniversityEntry(
                    rank++,
                    (String)  row[0],
                    ((Number) row[1]).intValue(),
                    ((Number) row[2]).intValue(),
                    ((Number) row[3]).doubleValue()
            ));
        }
        return result;
    }
}
