package com.StartUp.service;

import com.StartUp.entity.*;
import com.StartUp.repository.PaymentRepository;
import com.StartUp.repository.ReferralRepository;
import com.StartUp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ReferralRepository referralRepository;

    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.10");

    public BigDecimal calculateCommission(BigDecimal amount) {
        return amount.multiply(COMMISSION_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTotal(BigDecimal amount) {
        BigDecimal commission = calculateCommission(amount);
        return amount.add(commission).setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    public Payment createPayment(Long jobApplicationId, User payer, User receiver, BigDecimal amount) {
        BigDecimal commission = calculateCommission(amount);
        BigDecimal totalAmount = calculateTotal(amount);

        Payment payment = Payment.builder()
                .jobApplicationId(jobApplicationId)
                .payer(payer)
                .receiver(receiver)
                .amount(amount)
                .commission(commission)
                .totalAmount(totalAmount)
                .status(Payment.PaymentStatus.PENDING)
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        
        notificationService.createNotification(
                payer,
                Notification.NotificationType.PAYMENT_SENT,
                "Плащане създадено",
                "Създадохте плащане на стойност " + totalAmount + " лв.",
                savedPayment.getId(),
                "Payment"
        );

        return savedPayment;
    }

    @Transactional
    public Payment confirmPayment(Long paymentId, String transactionId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setTransactionId(transactionId);
        payment.setCompletedAt(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);

        notificationService.createNotification(
                payment.getReceiver(),
                Notification.NotificationType.PAYMENT_RECEIVED,
                "Получено плащане",
                "Получихте плащане на стойност " + payment.getAmount() + " лв.",
                savedPayment.getId(),
                "Payment"
        );

        return savedPayment;
    }

    public boolean isReferredUserFirstJob(User receiver) {
        Optional<Referral> referral = referralRepository.findByReferredId(receiver.getId());
        if (referral.isEmpty()) return false;

        List<Payment> previousPayments = paymentRepository.findByReceiverId(receiver.getId())
                .stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED)
                .toList();

        return previousPayments.isEmpty();
    }

    public List<Payment> getPaymentHistory(Long userId) {
        List<Payment> sentPayments = paymentRepository.findByPayerId(userId);
        List<Payment> receivedPayments = paymentRepository.findByReceiverId(userId);
        
        sentPayments.addAll(receivedPayments);
        return sentPayments;
    }

    public List<Payment> getPaymentsByStatus(Payment.PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    @Transactional
    public Payment updatePaymentStatus(Long paymentId, Payment.PaymentStatus status) {
        Payment payment = getPaymentById(paymentId);
        payment.setStatus(status);
        
        if (status == Payment.PaymentStatus.COMPLETED) {
            payment.setCompletedAt(LocalDateTime.now());
        }
        
        return paymentRepository.save(payment);
    }
}
