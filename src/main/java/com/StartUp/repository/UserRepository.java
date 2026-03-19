package com.StartUp.repository;

import com.StartUp.entity.User;
import com.StartUp.enums.Role;
import com.StartUp.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Long countByRole(Role role);
    Long countByStatus(UserStatus status);
    Page<User> findByStatus(UserStatus status, Pageable pageable);
    Optional<User> findByVerificationToken(String token);
    Optional<User> findByStripeAccountId(String stripeAccountId);
}
