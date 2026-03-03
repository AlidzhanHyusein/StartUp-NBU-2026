package com.StartUp.repository;

import com.StartUp.entity.Payment;
import com.StartUp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByPayerId(Long payerId);
    List<Payment> findByReceiverId(Long receiverId);
    List<Payment> findByPayer(User payer);
    List<Payment> findByReceiver(User receiver);
    List<Payment> findByStatus(Payment.PaymentStatus status);
}
