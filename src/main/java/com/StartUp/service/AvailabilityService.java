package com.StartUp.service;

import com.StartUp.dtos.availability.AvailabilityDtos;
import com.StartUp.dtos.availability.HeatmapDtos;
import com.StartUp.entity.Availability;
import com.StartUp.entity.StudentProfile;
import com.StartUp.entity.User;
import com.StartUp.enums.AvailabilityStatus;
import com.StartUp.exception.AppExceptions;
import com.StartUp.repository.AvailabilityRepository;
import com.StartUp.repository.StudentProfileRepository;
import com.StartUp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final AvailabilityRepository    availabilityRepository;
    private final StudentProfileRepository  studentProfileRepository;
    private final UserRepository            userRepository;


    private String currentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private StudentProfile currentStudent() {
        return studentProfileRepository.findByUser_Email(currentEmail())
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Student profile not found"));
    }

    private StudentProfile profileForUser(User user) {
        return studentProfileRepository.findByUserId(user.getId()).orElse(null);
    }


    @Transactional
    public AvailabilityDtos.AvailabilityResponse create(AvailabilityDtos.AvailabilityRequest request) {
        StudentProfile student = currentStudent();
        Availability slot = Availability.builder()
                .student(student.getUser())
                .date(request.date())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .status(request.status() != null ? request.status() : AvailabilityStatus.AVAILABLE)
                .note(request.note())
                .build();
        return mapToResponse(availabilityRepository.save(slot));
    }

    @Transactional
    public List<AvailabilityDtos.AvailabilityResponse> bulkCreate(
            List<AvailabilityDtos.AvailabilityRequest> requests) {
        StudentProfile student = currentStudent();
        List<Availability> slots = requests.stream()
                .map(r -> Availability.builder()
                        .student(student.getUser())
                        .date(r.date())
                        .startTime(r.startTime())
                        .endTime(r.endTime())
                        .status(r.status() != null ? r.status() : AvailabilityStatus.AVAILABLE)
                        .note(r.note())
                        .build())
                .toList();
        return availabilityRepository.saveAll(slots).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AvailabilityDtos.AvailabilityResponse> getMy() {
        StudentProfile student = currentStudent();
        return availabilityRepository
                .findByStudent_IdOrderByDateAscStartTimeAsc(student.getUser().getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public AvailabilityDtos.AvailabilityResponse update(Long id,
                                                         AvailabilityDtos.AvailabilityRequest request) {
        Availability slot = availabilityRepository.findById(id)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Availability slot not found"));
        StudentProfile student = currentStudent();
        if (!slot.getStudent().getId().equals(student.getUser().getId())) {
            throw new AppExceptions.UnauthorizedException("You do not own this slot");
        }
        slot.setDate(request.date());
        slot.setStartTime(request.startTime());
        slot.setEndTime(request.endTime());
        if (request.status() != null) slot.setStatus(request.status());
        slot.setNote(request.note());
        return mapToResponse(availabilityRepository.save(slot));
    }

    @Transactional
    public void delete(Long id) {
        Availability slot = availabilityRepository.findById(id)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Availability slot not found"));
        StudentProfile student = currentStudent();
        if (!slot.getStudent().getId().equals(student.getUser().getId())) {
            throw new AppExceptions.UnauthorizedException("You do not own this slot");
        }
        availabilityRepository.delete(slot);
    }


    @Transactional(readOnly = true)
    public List<AvailabilityDtos.AvailabilityResponse> getByStudent(Long studentProfileId) {
        StudentProfile sp = studentProfileRepository.findById(studentProfileId)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Student profile not found"));
        return availabilityRepository
                .findByStudent_IdOrderByDateAscStartTimeAsc(sp.getUser().getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AvailabilityDtos.AvailabilityResponse> getByStudentRange(
            Long studentProfileId, LocalDate startDate, LocalDate endDate) {
        StudentProfile sp = studentProfileRepository.findById(studentProfileId)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Student profile not found"));
        return availabilityRepository
                .findByStudent_IdAndDateBetweenOrderByDateAscStartTimeAsc(
                        sp.getUser().getId(), startDate, endDate)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AvailabilityDtos.AvailabilityResponse> getAvailableOnDate(LocalDate date) {
        return availabilityRepository
                .findByDateAndStatus(date, AvailabilityStatus.AVAILABLE)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    @Transactional(readOnly = true)
    public HeatmapDtos.HeatmapResponse getHeatmap(String city) {
        LocalDate today = LocalDate.now();

        List<Availability> slots = (city != null && !city.isBlank())
                ? availabilityRepository.findByStatusAndStudentProfile_CityIgnoreCaseAndDateGreaterThanEqual(
                        AvailabilityStatus.AVAILABLE, city.trim(), today)
                : availabilityRepository.findByStatusAndDateGreaterThanEqual(
                        AvailabilityStatus.AVAILABLE, today);

        if (slots.isEmpty()) {
            return new HeatmapDtos.HeatmapResponse(List.of(), 0L, 0, 9, city);
        }

        @SuppressWarnings("unchecked")
        Map<Long, HeatmapDtos.StudentSummary>[][] grid = new HashMap[7][24];
        for (int d = 0; d < 7; d++)
            for (int h = 0; h < 24; h++)
                grid[d][h] = new LinkedHashMap<>();

        for (Availability slot : slots) {
            int dow       = slot.getDate().getDayOfWeek().getValue() - 1;
            int startHour = slot.getStartTime().getHour();
            int endHour   = slot.getEndTime().getHour();
            if (slot.getEndTime().getMinute() == 0 && endHour > startHour) endHour--;

            User user   = slot.getStudent();
            long userId = user.getId();

            StudentProfile sp = profileForUser(user);

            HeatmapDtos.StudentSummary summary = new HeatmapDtos.StudentSummary(
                    userId,
                    user.getFirstName(),
                    user.getLastName(),
                    sp != null ? sp.getUniversity() : null,
                    sp != null ? sp.getCity()       : null
            );

            for (int h = startHour; h <= Math.min(endHour, 23); h++) {
                grid[dow][h].put(userId, summary);
            }
        }

        List<HeatmapDtos.HeatmapCell> cells = new ArrayList<>();
        long[] dayTotals = new long[7];
        int peakDay = 0, peakHour = 0;
        long peakVal = 0;

        for (int d = 0; d < 7; d++) {
            for (int h = 0; h < 24; h++) {
                Map<Long, HeatmapDtos.StudentSummary> cell = grid[d][h];
                if (cell.isEmpty()) continue;
                long count = cell.size();
                dayTotals[d] += count;
                List<HeatmapDtos.StudentSummary> studentList = cell.values().stream().limit(50).toList();
                cells.add(new HeatmapDtos.HeatmapCell(d, h, count, studentList));
                if (count > peakVal) { peakVal = count; peakDay = d; peakHour = h; }
            }
        }

        int peakDayByTotal = 0; long maxDayTotal = 0;
        for (int d = 0; d < 7; d++) {
            if (dayTotals[d] > maxDayTotal) { maxDayTotal = dayTotals[d]; peakDayByTotal = d; }
        }

        long totalStudents = slots.stream().map(s -> s.getStudent().getId()).distinct().count();

        return new HeatmapDtos.HeatmapResponse(cells, totalStudents, peakDayByTotal, peakHour, city);
    }


    private AvailabilityDtos.AvailabilityResponse mapToResponse(Availability a) {
        return new AvailabilityDtos.AvailabilityResponse(
                a.getId(),
                a.getDate(),
                a.getStartTime(),
                a.getEndTime(),
                a.getStatus(),
                a.getNote()
        );
    }
}
