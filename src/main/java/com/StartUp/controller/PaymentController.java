package com.StartUp.controller;

import com.StartUp.entity.Payment;
import com.StartUp.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/calculate")
    public ResponseEntity<Map<String, BigDecimal>> calculatePayment(@RequestBody Map<String, BigDecimal> request) {
        BigDecimal amount = request.get("amount");
        BigDecimal commission = paymentService.calculateCommission(amount);
        BigDecimal total = paymentService.calculateTotal(amount);

        return ResponseEntity.ok(Map.of(
                "amount", amount,
                "commission", commission,
                "total", total
        ));
    }

    @PostMapping("/{paymentId}/confirm")
    public ResponseEntity<Payment> confirmPayment(
            @PathVariable Long paymentId,
            @RequestBody Map<String, String> request) {
        String transactionId = request.get("transactionId");
        Payment payment = paymentService.confirmPayment(paymentId, transactionId);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<List<Payment>> getPaymentHistory(@PathVariable Long userId) {
        List<Payment> payments = paymentService.getPaymentHistory(userId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Payment>> getPaymentsByStatus(@PathVariable String status) {
        Payment.PaymentStatus paymentStatus = Payment.PaymentStatus.valueOf(status.toUpperCase());
        List<Payment> payments = paymentService.getPaymentsByStatus(paymentStatus);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        Payment payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(payment);
    }

    @PutMapping("/{paymentId}/status")
    public ResponseEntity<Payment> updatePaymentStatus(
            @PathVariable Long paymentId,
            @RequestBody Map<String, String> request) {
        Payment.PaymentStatus status = Payment.PaymentStatus.valueOf(request.get("status").toUpperCase());
        Payment payment = paymentService.updatePaymentStatus(paymentId, status);
        return ResponseEntity.ok(payment);
    }
}
