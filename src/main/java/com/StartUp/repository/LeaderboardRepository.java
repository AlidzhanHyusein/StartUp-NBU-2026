package com.StartUp.repository;

import com.StartUp.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LeaderboardRepository extends JpaRepository<Application, Long> {

    @Query("""
        SELECT
            sp.id,
            u.firstName,
            u.lastName,
            sp.university,
            sp.city,
            COUNT(a.id),
            SUM(COALESCE(j.salary, 0) * COALESCE(j.duration, 0))
        FROM Application a
        JOIN a.student sp
        JOIN sp.user u
        JOIN a.job j
        WHERE a.status = 'COMPLETED'
          AND a.appliedAt >= :from
          AND a.appliedAt < :to
        GROUP BY sp.id, u.firstName, u.lastName, sp.university, sp.city
        ORDER BY COUNT(a.id) DESC, SUM(COALESCE(j.salary, 0) * COALESCE(j.duration, 0)) DESC
        """)
    List<Object[]> findTopStudentsInPeriod(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query("""
        SELECT
            sp.id,
            u.firstName,
            u.lastName,
            sp.university,
            sp.city,
            COUNT(a.id),
            SUM(COALESCE(j.salary, 0) * COALESCE(j.duration, 0))
        FROM Application a
        JOIN a.student sp
        JOIN sp.user u
        JOIN a.job j
        WHERE a.status = 'COMPLETED'
        GROUP BY sp.id, u.firstName, u.lastName, sp.university, sp.city
        ORDER BY COUNT(a.id) DESC, SUM(COALESCE(j.salary, 0) * COALESCE(j.duration, 0)) DESC
        """)
    List<Object[]> findTopStudentsAllTime();


    @Query("""
        SELECT
            sp.university,
            COUNT(DISTINCT sp.id),
            COUNT(a.id),
            SUM(COALESCE(j.salary, 0) * COALESCE(j.duration, 0))
        FROM Application a
        JOIN a.student sp
        JOIN a.job j
        WHERE a.status = 'COMPLETED'
          AND a.appliedAt >= :from
          AND a.appliedAt < :to
          AND sp.university IS NOT NULL
        GROUP BY sp.university
        ORDER BY COUNT(a.id) DESC
        """)
    List<Object[]> findUniversityStatsInPeriod(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);


    @Query("""
        SELECT
            sp.university,
            COUNT(DISTINCT sp.id),
            COUNT(a.id),
            SUM(COALESCE(j.salary, 0) * COALESCE(j.duration, 0))
        FROM Application a
        JOIN a.student sp
        JOIN a.job j
        WHERE a.status = 'COMPLETED'
          AND sp.university IS NOT NULL
        GROUP BY sp.university
        ORDER BY COUNT(a.id) DESC
        """)
    List<Object[]> findUniversityStatsAllTime();
}
