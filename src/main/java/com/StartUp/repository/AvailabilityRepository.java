package com.StartUp.repository;

import com.StartUp.entity.Availability;
import com.StartUp.enums.AvailabilityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {


    List<Availability> findByStudent_IdOrderByDateAscStartTimeAsc(Long userId);

    List<Availability> findByStudent_IdAndDateBetweenOrderByDateAscStartTimeAsc(
            Long userId, LocalDate startDate, LocalDate endDate);


    List<Availability> findByDateAndStatus(LocalDate date, AvailabilityStatus status);


    @Query("""
        SELECT a FROM Availability a
        JOIN FETCH a.student u
        WHERE a.status = :status
          AND a.date >= :fromDate
        ORDER BY a.date ASC
        """)
    List<Availability> findByStatusAndDateGreaterThanEqual(
            @Param("status") AvailabilityStatus status,
            @Param("fromDate") LocalDate fromDate);


    @Query("""
        SELECT a FROM Availability a
        JOIN FETCH a.student u
        JOIN u.studentProfile sp
        WHERE a.status = :status
          AND LOWER(sp.city) = LOWER(:city)
          AND a.date >= :fromDate
        ORDER BY a.date ASC
        """)
    List<Availability> findByStatusAndStudentProfile_CityIgnoreCaseAndDateGreaterThanEqual(
            @Param("status") AvailabilityStatus status,
            @Param("city") String city,
            @Param("fromDate") LocalDate fromDate);
}
