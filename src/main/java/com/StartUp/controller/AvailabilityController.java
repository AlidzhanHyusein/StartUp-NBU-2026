package com.StartUp.controller;

import com.StartUp.dtos.availability.AvailabilityDtos;
import com.StartUp.dtos.availability.HeatmapDtos;
import com.StartUp.service.AvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
@Tag(name = "Availability", description = "Student availability management + employer heatmap")
public class AvailabilityController {

    private final AvailabilityService availabilityService;


    @Operation(summary = "Create a single availability slot (student)")
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AvailabilityDtos.AvailabilityResponse> create(
            @RequestBody AvailabilityDtos.AvailabilityRequest request) {
        return ResponseEntity.ok(availabilityService.create(request));
    }

    @Operation(summary = "Create multiple availability slots at once (student)")
    @PostMapping("/bulk")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<AvailabilityDtos.AvailabilityResponse>> bulkCreate(
            @RequestBody List<AvailabilityDtos.AvailabilityRequest> requests) {
        return ResponseEntity.ok(availabilityService.bulkCreate(requests));
    }

    @Operation(summary = "Get my availability slots (student)")
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<AvailabilityDtos.AvailabilityResponse>> getMy() {
        return ResponseEntity.ok(availabilityService.getMy());
    }

    @Operation(summary = "Update an availability slot (student)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AvailabilityDtos.AvailabilityResponse> update(
            @PathVariable Long id,
            @RequestBody AvailabilityDtos.AvailabilityRequest request) {
        return ResponseEntity.ok(availabilityService.update(id, request));
    }

    @Operation(summary = "Delete an availability slot (student)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        availabilityService.delete(id);
        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "Get a student's availability slots (employer)")
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<List<AvailabilityDtos.AvailabilityResponse>> getByStudent(
            @PathVariable Long studentId) {
        return ResponseEntity.ok(availabilityService.getByStudent(studentId));
    }

    @Operation(summary = "Get a student's availability within a date range (employer)")
    @GetMapping("/student/{studentId}/range")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<List<AvailabilityDtos.AvailabilityResponse>> getByStudentRange(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(availabilityService.getByStudentRange(studentId, startDate, endDate));
    }

    @Operation(summary = "Get all students available on a specific date (employer)")
    @GetMapping("/available")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<List<AvailabilityDtos.AvailabilityResponse>> getAvailableOnDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(availabilityService.getAvailableOnDate(date));
    }


    @Operation(
        summary = "Live availability heatmap (employer)",
        description = """
            Returns a day-of-week × hour grid showing how many students are AVAILABLE
            at each time slot. Optionally filter by city.

            dayOfWeek:  0 = Monday … 6 = Sunday
            hour:       0–23 (24-hour clock)

            Use this before posting a shift to pick the time slot with the most
            available students in your city.
            """
    )
    @GetMapping("/heatmap")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<HeatmapDtos.HeatmapResponse> getHeatmap(
            @RequestParam(required = false) String city) {
        return ResponseEntity.ok(availabilityService.getHeatmap(city));
    }
}
