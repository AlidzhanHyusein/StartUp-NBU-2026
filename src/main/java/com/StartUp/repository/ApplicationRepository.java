package com.StartUp.repository;

import com.StartUp.entity.Application;
import com.StartUp.entity.Job;
import com.StartUp.entity.StudentProfile;
import com.StartUp.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application,Long> {

    @Query(value = "SELECT a FROM Application a JOIN FETCH a.student s JOIN FETCH s.user JOIN FETCH a.job WHERE a.status = :status",
            countQuery = "SELECT COUNT(a) FROM Application a WHERE a.status = :status"
    )
    Page<Application> findAllByStatus(@Param("status") ApplicationStatus status, Pageable pageable);

    boolean existsByStudentAndJob(StudentProfile studentProfile, Job job);
}
