package com.StartUp.service;

import com.StartUp.dtos.shiftswap.ShiftSwapDtos;
import com.StartUp.entity.*;
import com.StartUp.enums.ApplicationStatus;
import com.StartUp.enums.ShiftSwapStatus;
import com.StartUp.exception.AppExceptions;
import com.StartUp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShiftSwapService {

    private final ShiftSwapRepository       shiftSwapRepository;
    private final ApplicationRepository     applicationRepository;
    private final StudentProfileRepository  studentProfileRepository;
    private final EmployerProfileRepository employerProfileRepository;


    private String currentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private StudentProfile currentStudent() {
        return studentProfileRepository.findByUser_Email(currentEmail())
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Student profile not found"));
    }

    private EmployerProfile currentEmployer() {
        return employerProfileRepository.findByUser_Email(currentEmail())
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Employer profile not found"));
    }


    @Transactional
    public ShiftSwapDtos.ShiftSwapResponse postSwap(ShiftSwapDtos.PostSwapRequest request) {
        StudentProfile poster = currentStudent();

        Application application = applicationRepository.findById(request.applicationId())
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Application not found"));

        if (!application.getStudent().getId().equals(poster.getId())) {
            throw new AppExceptions.UnauthorizedException("You do not own this application");
        }

        if (application.getStatus() != ApplicationStatus.ACCEPTED) {
            throw new AppExceptions.BadRequestException("Only ACCEPTED applications can be swapped");
        }

        if (shiftSwapRepository.existsByApplication_IdAndStatusIn(
                application.getId(),
                List.of(ShiftSwapStatus.OPEN, ShiftSwapStatus.CLAIMED))) {
            throw new AppExceptions.BadRequestException("An active swap already exists for this application");
        }

        ShiftSwap swap = ShiftSwap.builder()
                .application(application)
                .poster(poster)
                .status(ShiftSwapStatus.OPEN)
                .reason(request.reason())
                .build();

        return mapToResponse(shiftSwapRepository.save(swap));
    }


    @Transactional(readOnly = true)
    public List<ShiftSwapDtos.ShiftSwapResponse> getMarketplace() {
        StudentProfile student = currentStudent();
        return shiftSwapRepository.findOpenSwapsNotByStudent(student.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    @Transactional
    public ShiftSwapDtos.ShiftSwapResponse claimSwap(Long swapId) {
        StudentProfile claimer = currentStudent();
        ShiftSwap swap = findById(swapId);

        if (swap.getStatus() != ShiftSwapStatus.OPEN) {
            throw new AppExceptions.BadRequestException("This swap is no longer available");
        }
        if (swap.getPoster().getId().equals(claimer.getId())) {
            throw new AppExceptions.BadRequestException("You cannot claim your own swap");
        }

        swap.setClaimer(claimer);
        swap.setStatus(ShiftSwapStatus.CLAIMED);
        swap.setClaimedAt(LocalDateTime.now());
        return mapToResponse(shiftSwapRepository.save(swap));
    }


    @Transactional
    public ShiftSwapDtos.ShiftSwapResponse cancelSwap(Long swapId) {
        StudentProfile student = currentStudent();
        ShiftSwap swap = findById(swapId);

        if (!swap.getPoster().getId().equals(student.getId())) {
            throw new AppExceptions.UnauthorizedException("You did not post this swap");
        }
        if (swap.getStatus() != ShiftSwapStatus.OPEN) {
            throw new AppExceptions.BadRequestException("Only OPEN swaps can be cancelled");
        }

        swap.setStatus(ShiftSwapStatus.CANCELLED);
        swap.setResolvedAt(LocalDateTime.now());
        return mapToResponse(shiftSwapRepository.save(swap));
    }


    @Transactional(readOnly = true)
    public List<ShiftSwapDtos.ShiftSwapResponse> getMy() {
        StudentProfile student = currentStudent();
        List<ShiftSwap> posted  = shiftSwapRepository.findByPoster_IdOrderByCreatedAtDesc(student.getId());
        List<ShiftSwap> claimed = shiftSwapRepository.findByClaimer_IdOrderByCreatedAtDesc(student.getId());
        return java.util.stream.Stream.concat(posted.stream(), claimed.stream())
                .distinct()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    @Transactional
    public ShiftSwapDtos.ShiftSwapResponse resolveSwap(Long swapId, boolean approve) {
        EmployerProfile employer = currentEmployer();
        ShiftSwap swap = findById(swapId);

        if (!swap.getApplication().getJob().getEmployer().getId().equals(employer.getId())) {
            throw new AppExceptions.UnauthorizedException("You do not own this job");
        }
        if (swap.getStatus() != ShiftSwapStatus.CLAIMED) {
            throw new AppExceptions.BadRequestException("Only CLAIMED swaps can be approved/rejected");
        }

        if (approve) {
            Application application = swap.getApplication();
            application.setStudent(swap.getClaimer());
            application.setFirstName(swap.getClaimer().getUser().getFirstName());
            application.setLastName(swap.getClaimer().getUser().getLastName());
            application.setEmail(swap.getClaimer().getUser().getEmail());
            applicationRepository.save(application);
            swap.setStatus(ShiftSwapStatus.APPROVED);
        } else {
            swap.setClaimer(null);
            swap.setClaimedAt(null);
            swap.setStatus(ShiftSwapStatus.OPEN);
        }

        swap.setResolvedAt(approve ? LocalDateTime.now() : null);
        return mapToResponse(shiftSwapRepository.save(swap));
    }


    @Transactional(readOnly = true)
    public List<ShiftSwapDtos.ShiftSwapResponse> getPendingForEmployer() {
        EmployerProfile employer = currentEmployer();
        return shiftSwapRepository.findPendingSwapsForEmployer(employer.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    private ShiftSwap findById(Long id) {
        return shiftSwapRepository.findById(id)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Swap not found"));
    }

    private ShiftSwapDtos.ShiftSwapResponse mapToResponse(ShiftSwap ss) {
        Job job = ss.getApplication().getJob();
        return new ShiftSwapDtos.ShiftSwapResponse(
                ss.getId(),
                new ShiftSwapDtos.JobInfo(
                        job.getId(),
                        job.getTitle(),
                        job.getEmployer() != null ? job.getEmployer().getCompanyName() : "",
                        job.getCategory() != null ? job.getCategory().name() : "",
                        job.getType() != null ? job.getType().name() : "",
                        job.getLocation() != null ? job.getLocation().name() : "",
                        job.getStartDate() != null ? job.getStartDate().toString() : "",
                        job.getEndDate() != null ? job.getEndDate().toString() : "",
                        job.getSalary() != null ? job.getSalary().doubleValue() : 0
                ),
                mapStudent(ss.getPoster()),
                mapStudent(ss.getClaimer()),
                ss.getStatus(),
                ss.getReason(),
                ss.getCreatedAt(),
                ss.getClaimedAt(),
                ss.getResolvedAt()
        );
    }

    private ShiftSwapDtos.StudentInfo mapStudent(StudentProfile sp) {
        if (sp == null) return null;
        return new ShiftSwapDtos.StudentInfo(
                sp.getId(),
                sp.getUser().getFirstName(),
                sp.getUser().getLastName(),
                sp.getUniversity(),
                sp.getCity()
        );
    }
}
