package com.StartUp.service;

import com.StartUp.entity.Job;
import com.StartUp.enums.ApplicationStatus;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @Async
    public void sendVerificationEmail(String toEmail, String token) {
        String link = baseUrl + "/api/auth/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Verify your email");
        message.setText("Click to verify your account: " + link);
        mailSender.send(message);
    }

    @Async
    public void sendApplicationStatusEmail(String email, ApplicationStatus status, Job job) {
        String subject;
        String text;

        if (status == ApplicationStatus.ACCEPTED) {
            subject = "Congratulations! Your application was accepted 🎉";
            text = String.format(
                    "Great news! Your application for the position '%s' at '%s' has been accepted.\nThe employer will contact you soon.",
                    job.getTitle(), job.getEmployer().getCompanyName()
            );
        } else if (status == ApplicationStatus.REJECTED) {
            subject = "Your application was not successful";
            text = String.format(
                    "Unfortunately your application for the position '%s' at '%s' has been rejected.\nKeep applying and good luck!",
                    job.getTitle(), job.getEmployer().getCompanyName()
            );
        } else {
            subject = "Your application status has changed";
            text = String.format(
                    "Your application for the position '%s' at '%s' has been updated to: %s",
                    job.getTitle(), job.getEmployer().getCompanyName(), status
            );
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}