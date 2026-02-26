package com.StartUp.repository;

import com.StartUp.enums.Role;
import com.StartUp.entity.User;
import com.StartUp.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {



    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByRole(Role role, Pageable pageable);

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    long countByRole(Role role);

    long countByStatus(UserStatus status);

    @Query("SELECT COUNT(u) FROM User u WHERE u.status = 'PENDING'")
    long countPendingUsers();
}
