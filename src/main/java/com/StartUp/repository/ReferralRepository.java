package com.StartUp.repository;

import com.StartUp.entity.Referral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReferralRepository extends JpaRepository<Referral, Long> {

    Optional<Referral> findByReferralCode(String referralCode);

    List<Referral> findByReferrerIdOrderByCreatedAtDesc(Long referrerId);

    Optional<Referral> findByInvitedEmail(String email);

    boolean existsByReferrerIdAndInvitedEmail(Long referrerId, String invitedEmail);

    Optional<Referral> findByReferredId(Long referredId);

    @Query("SELECT COUNT(r) FROM Referral r WHERE r.referrer.id = :referrerId AND r.status = 'COMPLETED'")
    Long countCompletedByReferrer(@Param("referrerId") Long referrerId);

    @Query("SELECT COUNT(r) FROM Referral r WHERE r.referrer.id = :referrerId AND r.bonusPaid = true")
    Long countRewardedByReferrer(@Param("referrerId") Long referrerId);
}