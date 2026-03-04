package com.StartUp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {


    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String toEmail, String token) {
        String link = "http://localhost:8080/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Verify your email");
        message.setText("Click to verify your account: " + link);

        mailSender.send(message);
    }
}