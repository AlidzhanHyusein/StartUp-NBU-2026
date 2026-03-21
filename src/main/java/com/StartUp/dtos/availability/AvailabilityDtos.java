package com.StartUp.dtos.availability;

import com.StartUp.enums.AvailabilityStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public class AvailabilityDtos {


    public record AvailabilityRequest(
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            AvailabilityStatus status,
            String note
    ) {}

    public record AvailabilityResponse(
            Long id,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            AvailabilityStatus status,
            String note
    ) {}
}
