package com.StartUp.controller;

import com.StartUp.dtos.referral.ReferralDtos;
import com.StartUp.service.ReferralService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/referrals")
@RequiredArgsConstructor
public class ReferralController {

    private final ReferralService referralService;

    @Operation(summary = "Get my referral code and share link")
    @GetMapping("/my-code")
    public ResponseEntity<ReferralDtos.ReferralCodeResponse> getMyCode(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(referralService.getMyReferralCode(userDetails.getUsername()));
    }

    @Operation(summary = "Invite a friend by email")
    @PostMapping("/invite")
    public ResponseEntity<ReferralDtos.ReferralResponse> invite(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ReferralDtos.InviteRequest request) {
        return ResponseEntity.ok(referralService.inviteFriend(userDetails.getUsername(), request));
    }

    @Operation(summary = "Get my referral history")
    @GetMapping("/my")
    public ResponseEntity<List<ReferralDtos.ReferralResponse>> getMyReferrals(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(referralService.getMyReferrals(userDetails.getUsername()));
    }

    @Operation(summary = "Get my referral stats and earnings")
    @GetMapping("/stats")
    public ResponseEntity<ReferralDtos.ReferralStatsResponse> getStats(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(referralService.getMyStats(userDetails.getUsername()));
    }
}