package com.StartUp.entity;

import com.StartUp.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private StudentProfile student;

    private String email;

    private String firstName;

    private String lastName;

    private String city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private Job job;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    private LocalDate appliedAt;

    private String resumeUrl;

    @Column(columnDefinition = "TEXT")
    private String messageToCompany;

    @PrePersist
    public void onApplied(){
        this.appliedAt = LocalDate.now();
    }
}
