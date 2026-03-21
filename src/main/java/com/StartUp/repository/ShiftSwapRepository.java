package com.StartUp.repository;

import com.StartUp.entity.ShiftSwap;
import com.StartUp.enums.ShiftSwapStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftSwapRepository extends JpaRepository<ShiftSwap, Long> {

    @Query("""
        SELECT ss FROM ShiftSwap ss
        JOIN FETCH ss.poster p
        JOIN FETCH ss.application a
        JOIN FETCH a.job j
        WHERE ss.status = 'OPEN'
          AND ss.poster.id <> :currentStudentId
        ORDER BY ss.createdAt DESC
        """)
    List<ShiftSwap> findOpenSwapsNotByStudent(@Param("currentStudentId") Long currentStudentId);

    List<ShiftSwap> findByPoster_IdOrderByCreatedAtDesc(Long posterId);

    List<ShiftSwap> findByClaimer_IdOrderByCreatedAtDesc(Long claimerId);

    @Query("""
        SELECT ss FROM ShiftSwap ss
        JOIN FETCH ss.application a
        JOIN FETCH a.job j
        WHERE j.employer.id = :employerId
          AND ss.status = 'CLAIMED'
        ORDER BY ss.claimedAt DESC
        """)
    List<ShiftSwap> findPendingSwapsForEmployer(@Param("employerId") Long employerId);

    Optional<ShiftSwap> findByApplication_Id(Long applicationId);

    boolean existsByApplication_IdAndStatusIn(Long applicationId, List<ShiftSwapStatus> statuses);
}
