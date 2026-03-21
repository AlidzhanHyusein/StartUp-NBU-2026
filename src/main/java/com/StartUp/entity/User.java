package com.StartUp.entity;

import com.StartUp.enums.Role;
import com.StartUp.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
@Data
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private StudentProfile studentProfile;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.PENDING;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "refresh_token")
    private String refreshToken;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "stripe_account_id")
    private String stripeAccountId;

    @Column(name = "stripe_onboarding_complete")
    @Builder.Default
    private Boolean stripeOnboardingComplete = false;

    private String verificationToken;

    private boolean enabled = false;

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }



    public void setRating(int rating) {

    }
}