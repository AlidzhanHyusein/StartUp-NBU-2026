package com.StartUp.dtos.referral;

import com.StartUp.entity.Referral;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReferralDtos {

    public record InviteRequest(String email) {}

    public record ReferralResponse(
            Long id,
            String referralCode,
            String invitedEmail,
            String referredName,
            Referral.ReferralStatus status,
            BigDecimal bonusAmount,
            Boolean bonusPaid,
            LocalDateTime createdAt,
            LocalDateTime completedAt
    ) {}

    public record ReferralStatsResponse(
            String myReferralCode,
            Long totalInvited,
            Long totalSignedUp,
            Long totalCompleted,
            Long totalRewarded,
            BigDecimal totalEarned
    ) {}

    public record ReferralCodeResponse(
            String referralCode,
            String shareLink
    ) {}
}