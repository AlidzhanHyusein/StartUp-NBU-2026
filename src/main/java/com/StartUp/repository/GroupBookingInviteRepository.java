package com.StartUp.repository;

import com.StartUp.entity.GroupBookingInvite;
import com.StartUp.enums.GroupInviteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupBookingInviteRepository extends JpaRepository<GroupBookingInvite, Long> {

    List<GroupBookingInvite> findByGroupBooking_Id(Long groupBookingId);

    @Query("""
        SELECT i FROM GroupBookingInvite i
        JOIN FETCH i.groupBooking gb
        JOIN FETCH gb.job j
        JOIN FETCH gb.leader l
        JOIN FETCH l.user lu
        WHERE i.student.id = :studentId
          AND i.status = :status
        ORDER BY i.createdAt DESC
        """)
    List<GroupBookingInvite> findByStudent_IdAndStatus(
            @Param("studentId") Long studentId,
            @Param("status") GroupInviteStatus status);

    @Query("""
        SELECT i FROM GroupBookingInvite i
        JOIN FETCH i.groupBooking gb
        JOIN FETCH gb.job j
        JOIN FETCH gb.leader l
        JOIN FETCH l.user lu
        WHERE i.student.id = :studentId
        ORDER BY i.createdAt DESC
        """)
    List<GroupBookingInvite> findByStudent_Id(@Param("studentId") Long studentId);

    Optional<GroupBookingInvite> findByGroupBooking_IdAndStudent_Id(Long bookingId, Long studentId);

    long countByGroupBooking_IdAndStatus(Long bookingId, GroupInviteStatus status);

    long countByGroupBooking_Id(Long bookingId);
}
