package com.StartUp.controller;

import com.StartUp.dtos.leaderboard.LeaderboardDtos;
import com.StartUp.service.LeaderboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
@Tag(name = "Leaderboard", description = "Monthly and all-time student earnings leaderboard")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @Operation(summary = "Current month leaderboard (authenticated)")
    @GetMapping("/monthly")
    public ResponseEntity<LeaderboardDtos.LeaderboardResponse> getMonthly(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        LocalDate now = LocalDate.now();
        int y = (year  != null) ? year  : now.getYear();
        int m = (month != null) ? month : now.getMonthValue();
        return ResponseEntity.ok(leaderboardService.getMonthly(y, m));
    }

    @Operation(summary = "All-time leaderboard (authenticated)")
    @GetMapping("/all-time")
    public ResponseEntity<LeaderboardDtos.LeaderboardResponse> getAllTime() {
        return ResponseEntity.ok(leaderboardService.getAllTime());
    }
}
