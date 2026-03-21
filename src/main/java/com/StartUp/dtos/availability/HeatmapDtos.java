package com.StartUp.dtos.availability;

import java.util.List;

public class HeatmapDtos {

    public record HeatmapResponse(
            List<HeatmapCell> cells,
            long totalStudents,
            int peakDay,
            int peakHour,
            String city
    ) {}

    public record HeatmapCell(
            int dayOfWeek,
            int hour,
            long count,
            List<StudentSummary> students
    ) {}


    public record StudentSummary(
            Long id,
            String firstName,
            String lastName,
            String university,
            String city
    ) {}
}
