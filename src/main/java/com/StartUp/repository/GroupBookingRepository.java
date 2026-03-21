package com.StartUp.repository;

import com.StartUp.entity.GroupBooking;
import com.StartUp.enums.GroupBookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupBookingRepository extends JpaRepository<GroupBooking, Long> {

    List<GroupBooking> findByLeader_IdOrderByCreatedAtDesc(Long leaderId);

    @Query("""
        SELECT gb FROM GroupBooking gb
        JOIN gb.members m
        WHERE m.id = :studentId
        ORDER BY gb.createdAt DESC
        """)
    List<GroupBooking> findByMember_Id(@Param("studentId") Long studentId);

    @Query("""
        SELECT gb FROM GroupBooking gb
        JOIN FETCH gb.leader l
        JOIN FETCH l.user lu
        JOIN FETCH gb.job j
        WHERE j.employer.id = :employerId
        ORDER BY gb.createdAt DESC
        """)
    List<GroupBooking> findByEmployer_Id(@Param("employerId") Long employerId);

    List<GroupBooking> findByJob_IdOrderByCreatedAtDesc(Long jobId);

    @Query("""
        SELECT COUNT(gb) > 0 FROM GroupBooking gb
        JOIN gb.members m
        WHERE gb.job.id = :jobId
          AND m.id = :studentId
          AND gb.status NOT IN ('REJECTED', 'CANCELLED')
        """)
    boolean existsActiveGroupBookingForStudent(
            @Param("jobId") Long jobId,
            @Param("studentId") Long studentId);
}
