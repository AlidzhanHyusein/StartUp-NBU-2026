package com.StartUp.service;

import com.StartUp.entity.Payment;
import com.StartUp.entity.User;
import com.StartUp.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class StripeService {

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${app.base-url}")
    private String baseUrl;

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final NotificationService notificationService;

    @Transactional
    public String createCheckoutSession(User payer, User receiver, BigDecimal amount, Long jobApplicationId) throws Exception {
        Stripe.apiKey = secretKey;

        Payment payment = paymentService.createPayment(jobApplicationId, payer, receiver, amount);

        boolean noCommission = paymentService.isReferredUserFirstJob(receiver);

        BigDecimal commission = noCommission ? BigDecimal.ZERO : amount.multiply(new BigDecimal("0.10"));
        BigDecimal studentAmount = amount.subtract(commission);
        BigDecimal totalCharge = noCommission ? amount : amount.add(commission);

        long amountInCents = totalCharge.multiply(new BigDecimal("100")).longValue();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("https://breaddy.store/payments/success?session_id={CHECKOUT_SESSION_ID}&payment_id=" + payment.getId())
                .setCancelUrl("https://breaddy.store/payments/cancel")
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("eur")
                                .setUnitAmount(amountInCents)
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("Payment to " + receiver.getFullName())
                                        .build())
                                .build())
                        .build())
                .putMetadata("payment_id", payment.getId().toString())
                .putMetadata("payer_id", payer.getId().toString())
                .putMetadata("receiver_id", receiver.getId().toString())
                .putMetadata("student_amount", studentAmount.toString())
                .build();

        Session session = Session.create(params);
        return session.getUrl();
    }
}