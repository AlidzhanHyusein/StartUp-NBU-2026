package com.StartUp.controller;

import com.StartUp.entity.User;
import com.StartUp.repository.UserRepository;
import com.StartUp.service.PaymentService;
import com.StartUp.service.StripeConnectService;
import com.StartUp.service.StripeService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/payments/stripe")
@RequiredArgsConstructor
public class StripeController {

    private final StripeService stripeService;
    private final PaymentService paymentService;
    private final UserRepository userRepository;
    private final StripeConnectService stripeConnectService;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @PostMapping("/checkout")
    public ResponseEntity<Map<String, String>> createCheckout(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> request) throws Exception {

        User payer = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        User receiver = userRepository.findById(Long.valueOf(request.get("receiverId").toString())).orElseThrow();
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        Long jobApplicationId = Long.valueOf(request.get("jobApplicationId").toString());

        String checkoutUrl = stripeService.createCheckoutSession(payer, receiver, amount, jobApplicationId);

        return ResponseEntity.ok(Map.of("checkoutUrl", checkoutUrl));
    }
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

            if ("checkout.session.completed".equals(event.getType())) {
                Session session = (Session) event.getDataObjectDeserializer()
                        .getObject().orElseThrow();

                Long paymentId = Long.valueOf(session.getMetadata().get("payment_id"));
                Long receiverId = Long.valueOf(session.getMetadata().get("receiver_id"));
                BigDecimal studentAmount = new BigDecimal(session.getMetadata().get("student_amount"));
                String transactionId = session.getPaymentIntent();

                paymentService.confirmPayment(paymentId, transactionId);

                User student = userRepository.findById(receiverId).orElseThrow();
                stripeConnectService.transferToStudent(student, studentAmount);
            }

            return ResponseEntity.ok("OK");
        } catch (SignatureVerificationException e) {
            return ResponseEntity.badRequest().body("Invalid signature");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Webhook error: " + e.getMessage());
        }
    }
}