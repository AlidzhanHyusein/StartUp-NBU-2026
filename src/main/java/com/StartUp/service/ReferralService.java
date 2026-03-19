package com.StartUp.service;

import com.StartUp.dtos.referral.ReferralDtos;
import com.StartUp.entity.Notification;
import com.StartUp.entity.Referral;
import com.StartUp.entity.User;
import com.StartUp.exception.AppExceptions;
import com.StartUp.repository.ReferralRepository;
import com.StartUp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReferralService {

    private final ReferralRepository referralRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;

    @Value("${app.base-url}")
    private String baseUrl;

    private static final BigDecimal REFERRAL_BONUS = new BigDecimal("10.00");


    @Transactional
    public ReferralDtos.ReferralCodeResponse getMyReferralCode(String email) {
        User user = getUser(email);

        List<Referral> existing = referralRepository.findByReferrerIdOrderByCreatedAtDesc(user.getId());
        String code = existing.isEmpty()
                ? generateUniqueCode()
                : existing.get(0).getReferralCode();

        String shareLink = baseUrl.replace("api.", "") + "/auth-register?ref=" + code;

        return new ReferralDtos.ReferralCodeResponse(code, shareLink);
    }


    @Transactional
    public ReferralDtos.ReferralResponse inviteFriend(String referrerEmail, ReferralDtos.InviteRequest request) {
        User referrer = getUser(referrerEmail);

        if (referrer.getEmail().equalsIgnoreCase(request.email())) {
            throw new AppExceptions.BadRequestException("You cannot refer yourself.");
        }

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new AppExceptions.BadRequestException("This email is already registered.");
        }

        if (referralRepository.existsByReferrerIdAndInvitedEmail(referrer.getId(), request.email())) {
            throw new AppExceptions.BadRequestException("You already invited this email.");
        }

        String code = generateUniqueCode();

        Referral referral = Referral.builder()
                .referrer(referrer)
                .referralCode(code)
                .invitedEmail(request.email())
                .status(Referral.ReferralStatus.PENDING)
                .bonusAmount(REFERRAL_BONUS)
                .build();

        Referral saved = referralRepository.save(referral);

        emailService.sendReferralInviteEmail(request.email(), referrer.getFullName(), code, baseUrl);

        return mapToResponse(saved);
    }


    @Transactional
    public void handleRegistrationWithCode(String referralCode, User newUser) {
        referralRepository.findByReferralCode(referralCode).ifPresent(referral -> {
            referral.setReferred(newUser);
            referral.setStatus(Referral.ReferralStatus.SIGNED_UP);
            referralRepository.save(referral);

            notificationService.createNotification(
                    referral.getReferrer(),
                    Notification.NotificationType.SYSTEM,
                    "Friend joined Breaddy! 🎉",
                    newUser.getFullName() + " signed up using your referral link. They need to complete a shift for you to earn €" + REFERRAL_BONUS,
                    referral.getId(),
                    "Referral"
            );
        });

        referralRepository.findByInvitedEmail(newUser.getEmail()).ifPresent(referral -> {
            if (referral.getStatus() == Referral.ReferralStatus.PENDING) {
                referral.setReferred(newUser);
                referral.setStatus(Referral.ReferralStatus.SIGNED_UP);
                referralRepository.save(referral);
            }
        });
    }


    @Transactional
    public void handleFirstShiftCompleted(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("User not found"));

        referralRepository.findByReferrerIdOrderByCreatedAtDesc(userId)
                .stream()
                .filter(r -> r.getReferred() != null && r.getReferred().getId().equals(userId))
                .filter(r -> r.getStatus() == Referral.ReferralStatus.SIGNED_UP)
                .findFirst()
                .ifPresent(referral -> {
                    referral.setStatus(Referral.ReferralStatus.COMPLETED);
                    referral.setCompletedAt(LocalDateTime.now());
                    referralRepository.save(referral);

                    payReferralBonus(referral);
                });
    }


    @Transactional
    public void payReferralBonus(Referral referral) {
        if (Boolean.TRUE.equals(referral.getBonusPaid())) return;

        referral.setBonusPaid(true);
        referral.setStatus(Referral.ReferralStatus.REWARDED);
        referralRepository.save(referral);

        notificationService.createNotification(
                referral.getReferrer(),
                Notification.NotificationType.PAYMENT_RECEIVED,
                "Referral bonus earned! 🎁",
                "You earned €" + referral.getBonusAmount() + " because " + referral.getReferred().getFullName() + " completed their first shift!",
                referral.getId(),
                "Referral"
        );
    }


    public List<ReferralDtos.ReferralResponse> getMyReferrals(String email) {
        User user = getUser(email);
        return referralRepository.findByReferrerIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    public ReferralDtos.ReferralStatsResponse getMyStats(String email) {
        User user = getUser(email);
        List<Referral> referrals = referralRepository.findByReferrerIdOrderByCreatedAtDesc(user.getId());

        long totalInvited = referrals.size();
        long totalSignedUp = referrals.stream().filter(r -> r.getStatus() != Referral.ReferralStatus.PENDING).count();
        long totalCompleted = referralRepository.countCompletedByReferrer(user.getId());
        long totalRewarded = referralRepository.countRewardedByReferrer(user.getId());
        BigDecimal totalEarned = referrals.stream()
                .filter(r -> Boolean.TRUE.equals(r.getBonusPaid()))
                .map(Referral::getBonusAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ReferralDtos.ReferralCodeResponse codeResponse = getMyReferralCode(email);

        return new ReferralDtos.ReferralStatsResponse(
                codeResponse.referralCode(),
                totalInvited,
                totalSignedUp,
                totalCompleted,
                totalRewarded,
                totalEarned
        );
    }


    private String generateUniqueCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        } while (referralRepository.findByReferralCode(code).isPresent());
        return code;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("User not found"));
    }

    private ReferralDtos.ReferralResponse mapToResponse(Referral r) {
        String referredName = r.getReferred() != null ? r.getReferred().getFullName() : null;
        return new ReferralDtos.ReferralResponse(
                r.getId(),
                r.getReferralCode(),
                r.getInvitedEmail(),
                referredName,
                r.getStatus(),
                r.getBonusAmount(),
                r.getBonusPaid(),
                r.getCreatedAt(),
                r.getCompletedAt()
        );
    }
}