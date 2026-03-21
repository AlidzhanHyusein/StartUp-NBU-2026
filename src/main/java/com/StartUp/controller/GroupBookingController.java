package com.StartUp.controller;

import com.StartUp.dtos.groupbooking.GroupBookingDtos;
import com.StartUp.enums.GroupBookingStatus;
import com.StartUp.service.GroupBookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/group-bookings")
@RequiredArgsConstructor
@Tag(name = "Group Bookings", description = "Group applications with invite-accept-decline flow")
public class GroupBookingController {

    private final GroupBookingService groupBookingService;


    @Operation(summary = "Create a group booking and invite members (student — leader)")
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<GroupBookingDtos.GroupBookingResponse> create(
            @RequestBody GroupBookingDtos.CreateRequest request) {
        return ResponseEntity.ok(groupBookingService.create(request));
    }


    @Operation(summary = "Get all group bookings I'm involved in (student)")
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<GroupBookingDtos.GroupBookingResponse>> getMyGroups() {
        return ResponseEntity.ok(groupBookingService.getMyGroups());
    }


    @Operation(summary = "Get my pending group booking invites (student)")
    @GetMapping("/invites/pending")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<GroupBookingDtos.InviteResponse>> getMyPendingInvites() {
        return ResponseEntity.ok(groupBookingService.getMyPendingInvites());
    }


    @Operation(summary = "Accept a group booking invite (student)")
    @PostMapping("/{bookingId}/invites/accept")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<GroupBookingDtos.InviteResponse> acceptInvite(
            @PathVariable Long bookingId) {
        return ResponseEntity.ok(groupBookingService.respondToInvite(bookingId, true));
    }

    @Operation(summary = "Decline a group booking invite (student)")
    @PostMapping("/{bookingId}/invites/decline")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<GroupBookingDtos.InviteResponse> declineInvite(
            @PathVariable Long bookingId) {
        return ResponseEntity.ok(groupBookingService.respondToInvite(bookingId, false));
    }


    @Operation(summary = "Cancel a group booking (student — leader only)")
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<GroupBookingDtos.GroupBookingResponse> cancel(
            @PathVariable Long id) {
        return ResponseEntity.ok(groupBookingService.cancel(id));
    }


    @Operation(summary = "Get all group bookings for employer's jobs (employer)")
    @GetMapping("/employer")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<List<GroupBookingDtos.GroupBookingResponse>> getForEmployer() {
        return ResponseEntity.ok(groupBookingService.getForEmployer());
    }

    @Operation(summary = "Accept or reject a group booking (employer)")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<GroupBookingDtos.GroupBookingResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam GroupBookingStatus status) {
        return ResponseEntity.ok(groupBookingService.updateStatus(id, status));
    }
}
