package com.StartUp.service;

import com.StartUp.dtos.groupbooking.GroupBookingDtos;
import com.StartUp.entity.*;
import com.StartUp.enums.GroupBookingStatus;
import com.StartUp.enums.GroupInviteStatus;
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
public class GroupBookingService {

    private final GroupBookingRepository groupBookingRepository;
    private final GroupBookingInviteRepository inviteRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final JobRepository jobRepository;
    private final NotificationService notificationService;
    private final MessageService messageService;
    private final EmailService emailService;


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
    public GroupBookingDtos.GroupBookingResponse create(GroupBookingDtos.CreateRequest request) {
        StudentProfile leader = currentStudent();
        Job job = jobRepository.findById(request.jobId())
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Job not found"));

        if (job.getGroupSize() == null || job.getGroupSize() < 2) {
            throw new AppExceptions.BadRequestException("This job does not require group booking");
        }

        List<Long> invitedIds = request.memberProfileIds() == null
                ? List.of() : request.memberProfileIds();

        List<StudentProfile> invitees = invitedIds.stream()
                .filter(id -> !id.equals(leader.getId()))
                .map(id -> studentProfileRepository.findById(id)
                        .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Student not found: " + id)))
                .collect(Collectors.toList());

        int required = job.getGroupSize();
        if (invitees.size() + 1 > required) {
            throw new AppExceptions.BadRequestException(
                    "Too many members — this job needs exactly " + required + " students (including you)");
        }

        GroupBookingStatus initialStatus = invitees.isEmpty()
                ? GroupBookingStatus.PENDING
                : GroupBookingStatus.AWAITING_MEMBERS;

        GroupBooking booking = GroupBooking.builder()
                .job(job)
                .leader(leader)
                .requiredSize(required)
                .status(initialStatus)
                .message(request.message())
                .build();

        booking.getMembers().add(leader);

        GroupBooking saved = groupBookingRepository.save(booking);

        for (StudentProfile invitee : invitees) {
            GroupBookingInvite invite = GroupBookingInvite.builder()
                    .groupBooking(saved)
                    .student(invitee)
                    .status(GroupInviteStatus.PENDING)
                    .build();
            inviteRepository.save(invite);

            String jobTitle    = job.getTitle();
            String leaderName  = leader.getUser().getFirstName() + " " + leader.getUser().getLastName();
            notificationService.sendNotification(
                    invitee.getUser(),
                    "Group Booking Invite",
                    leaderName + " invited you to apply together for \"" + jobTitle + "\". " +
                    "Go to Group Bookings to accept or decline.",
                    "GROUP_INVITE",
                    saved.getId()
            );

            try {
                messageService.getOrCreateConversation(
                        leader.getUser().getId(),
                        invitee.getUser().getId()
                );
            } catch (Exception ignored) {
            }
        }

        return mapToResponse(saved);
    }


    @Transactional(readOnly = true)
    public List<GroupBookingDtos.GroupBookingResponse> getMyGroups() {
        StudentProfile student = currentStudent();
        List<GroupBooking> led      = groupBookingRepository.findByLeader_IdOrderByCreatedAtDesc(student.getId());
        List<GroupBooking> membered = groupBookingRepository.findByMember_Id(student.getId());
        return java.util.stream.Stream.concat(led.stream(), membered.stream())
                .distinct()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<GroupBookingDtos.InviteResponse> getMyPendingInvites() {
        StudentProfile student = currentStudent();
        return inviteRepository.findByStudent_IdAndStatus(student.getId(), GroupInviteStatus.PENDING)
                .stream()
                .map(this::mapToInviteResponse)
                .collect(Collectors.toList());
    }


    @Transactional
    public GroupBookingDtos.InviteResponse respondToInvite(Long bookingId, boolean accept) {
        StudentProfile student = currentStudent();

        GroupBookingInvite invite = inviteRepository
                .findByGroupBooking_IdAndStudent_Id(bookingId, student.getId())
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Invite not found"));

        if (invite.getStatus() != GroupInviteStatus.PENDING) {
            throw new AppExceptions.BadRequestException("You have already responded to this invite");
        }

        invite.setStatus(accept ? GroupInviteStatus.ACCEPTED : GroupInviteStatus.DECLINED);
        invite.setRespondedAt(LocalDateTime.now());
        inviteRepository.save(invite);

        GroupBooking booking = invite.getGroupBooking();
        String studentName = student.getUser().getFirstName() + " " + student.getUser().getLastName();

        if (accept) {
            booking.getMembers().add(student);

            long totalInvited  = inviteRepository.countByGroupBooking_Id(booking.getId());
            long totalAccepted = inviteRepository.countByGroupBooking_IdAndStatus(
                    booking.getId(), GroupInviteStatus.ACCEPTED);

            if (totalAccepted >= totalInvited) {
                booking.setStatus(GroupBookingStatus.PENDING);
                notificationService.sendNotification(
                        booking.getLeader().getUser(),
                        "Group Complete!",
                        "All members accepted. Your group for \"" + booking.getJob().getTitle()
                                + "\" is ready — the employer will now review it.",
                        "GROUP_COMPLETE",
                        booking.getId()
                );
            } else {
                notificationService.sendNotification(
                        booking.getLeader().getUser(),
                        "Group Invite Accepted",
                        studentName + " accepted your group booking invite for \""
                                + booking.getJob().getTitle() + "\".",
                        "GROUP_INVITE_ACCEPTED",
                        booking.getId()
                );
            }
        } else {
            notificationService.sendNotification(
                    booking.getLeader().getUser(),
                    "Group Invite Declined",
                    studentName + " declined your group booking invite for \""
                            + booking.getJob().getTitle() + "\". You can invite another student.",
                    "GROUP_INVITE_DECLINED",
                    booking.getId()
            );
        }

        groupBookingRepository.save(booking);
        return mapToInviteResponse(invite);
    }


    @Transactional
    public GroupBookingDtos.GroupBookingResponse cancel(Long id) {
        GroupBooking booking = findById(id);
        StudentProfile student = currentStudent();
        if (!booking.getLeader().getId().equals(student.getId())) {
            throw new AppExceptions.UnauthorizedException("Only the group leader can cancel");
        }
        if (booking.getStatus() == GroupBookingStatus.ACCEPTED) {
            throw new AppExceptions.BadRequestException("Cannot cancel an accepted booking");
        }
        booking.setStatus(GroupBookingStatus.CANCELLED);

        String leaderName = student.getUser().getFirstName() + " " + student.getUser().getLastName();
        for (StudentProfile member : booking.getMembers()) {
            if (!member.getId().equals(student.getId())) {
                notificationService.sendNotification(
                        member.getUser(),
                        "Group Booking Cancelled",
                        leaderName + " cancelled the group booking for \"" + booking.getJob().getTitle() + "\".",
                        "GROUP_CANCELLED",
                        booking.getId()
                );
            }
        }

        return mapToResponse(groupBookingRepository.save(booking));
    }


    @Transactional(readOnly = true)
    public List<GroupBookingDtos.GroupBookingResponse> getForEmployer() {
        EmployerProfile employer = currentEmployer();
        return groupBookingRepository.findByEmployer_Id(employer.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public GroupBookingDtos.GroupBookingResponse updateStatus(Long id, GroupBookingStatus status) {
        GroupBooking booking = findById(id);
        EmployerProfile employer = currentEmployer();
        if (!booking.getJob().getEmployer().getId().equals(employer.getId())) {
            throw new AppExceptions.UnauthorizedException("You do not own this job");
        }
        if (booking.getStatus() == GroupBookingStatus.AWAITING_MEMBERS) {
            throw new AppExceptions.BadRequestException("Cannot decide yet — waiting for all members to accept");
        }
        booking.setStatus(status);

        String jobTitle     = booking.getJob().getTitle();
        String companyName  = booking.getJob().getEmployer().getCompanyName();

        for (StudentProfile member : booking.getMembers()) {
            String email     = member.getUser().getEmail();
            String firstName = member.getUser().getFirstName();

            notificationService.sendNotification(
                    member.getUser(),
                    "Group Booking " + (status == GroupBookingStatus.ACCEPTED ? "Accepted!" : "Rejected"),
                    "Your group application for \"" + jobTitle + "\" was "
                            + (status == GroupBookingStatus.ACCEPTED ? "accepted" : "rejected")
                            + " by the employer.",
                    "GROUP_EMPLOYER_DECISION",
                    booking.getId()
            );

            if (status == GroupBookingStatus.ACCEPTED) {
                emailService.sendGroupBookingAccepted(email, firstName, jobTitle, companyName);
            } else if (status == GroupBookingStatus.REJECTED) {
                emailService.sendGroupBookingRejected(email, firstName, jobTitle, companyName);
            }
        }

        return mapToResponse(groupBookingRepository.save(booking));
    }

    private GroupBooking findById(Long id) {
        return groupBookingRepository.findById(id)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Group booking not found"));
    }

    private GroupBookingDtos.GroupBookingResponse mapToResponse(GroupBooking gb) {
        Job job = gb.getJob();
        List<GroupBookingDtos.InviteInfo> invites = inviteRepository
                .findByGroupBooking_Id(gb.getId())
                .stream()
                .map(i -> new GroupBookingDtos.InviteInfo(
                        i.getId(),
                        mapStudent(i.getStudent()),
                        i.getStatus(),
                        i.getRespondedAt()
                ))
                .collect(Collectors.toList());

        long totalInvited  = inviteRepository.countByGroupBooking_Id(gb.getId());
        long totalAccepted = inviteRepository.countByGroupBooking_IdAndStatus(
                gb.getId(), GroupInviteStatus.ACCEPTED);
        boolean allAccepted = totalInvited > 0 && totalAccepted >= totalInvited;

        return new GroupBookingDtos.GroupBookingResponse(
                gb.getId(),
                new GroupBookingDtos.JobInfo(
                        job.getId(),
                        job.getTitle(),
                        job.getEmployer() != null ? job.getEmployer().getCompanyName() : "",
                        job.getCategory() != null ? job.getCategory().name() : "",
                        job.getLocation() != null ? job.getLocation().name() : "",
                        gb.getRequiredSize()
                ),
                mapStudent(gb.getLeader()),
                gb.getMembers().stream().map(this::mapStudent).collect(Collectors.toList()),
                invites,
                gb.getRequiredSize(),
                gb.getMembers().size(),
                allAccepted,
                gb.getStatus(),
                gb.getMessage(),
                gb.getCreatedAt()
        );
    }

    private GroupBookingDtos.InviteResponse mapToInviteResponse(GroupBookingInvite i) {
        GroupBooking gb = i.getGroupBooking();
        Job job = gb.getJob();
        return new GroupBookingDtos.InviteResponse(
                i.getId(),
                gb.getId(),
                new GroupBookingDtos.JobInfo(
                        job.getId(),
                        job.getTitle(),
                        job.getEmployer() != null ? job.getEmployer().getCompanyName() : "",
                        job.getCategory() != null ? job.getCategory().name() : "",
                        job.getLocation() != null ? job.getLocation().name() : "",
                        gb.getRequiredSize()
                ),
                mapStudent(gb.getLeader()),
                gb.getMessage(),
                i.getStatus(),
                i.getCreatedAt()
        );
    }

    private GroupBookingDtos.StudentInfo mapStudent(StudentProfile sp) {
        if (sp == null) return null;
        return new GroupBookingDtos.StudentInfo(
                sp.getId(),
                sp.getUser().getId(),
                sp.getUser().getFirstName(),
                sp.getUser().getLastName(),
                sp.getUniversity(),
                sp.getCity()
        );
    }
}
