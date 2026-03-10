package com.StartUp.entity;

import com.StartUp.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "job_applications",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"student_id","job_id"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    private String resumeUrl;

    @Column(name = "message_to_company", columnDefinition = "TEXT")
    private String messageToCompany;

    @Column(name = "applied_at", nullable = false)
    private LocalDateTime appliedAt;

    @PrePersist
    public void onApplied(){
        this.appliedAt = LocalDateTime.now();
    }
}