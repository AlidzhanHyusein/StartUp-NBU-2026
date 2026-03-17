package com.StartUp.service;

import com.StartUp.entity.Job;
import com.StartUp.enums.ApplicationStatus;

import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.client.MailgunClient;
import com.mailgun.model.message.Message;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${mailgun.api-key}")
    private String apiKey;

    @Value("${mailgun.domain}")
    private String domain;

    @Value("${app.base-url}")
    private String baseUrl;

    @Async
    public void sendVerificationEmail(String toEmail, String token) {
        String link = baseUrl + "/api/auth/verify?token=" + token;

        MailgunMessagesApi mailgunMessagesApi = MailgunClient.config(apiKey)
                .createApi(MailgunMessagesApi.class);

        String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>"
                + "<h2 style='color: #4caf85;'>Welcome to Breaddy!</h2>"
                + "<p>Thank you for registering. Please verify your email address by clicking the button below:</p>"
                + "<a href='" + link + "' style='background-color: #4caf85; color: white; padding: 12px 24px; "
                + "text-decoration: none; border-radius: 8px; display: inline-block; font-weight: bold;'>"
                + "Verify Email</a>"
                + "<p style='color: #666; margin-top: 20px;'>If you did not create an account, please ignore this email.</p>"
                + "</div>";

        Message message = Message.builder()
                .from("Breaddy <noreply@mail.breaddy.store>")
                .to(toEmail)
                .subject("Verify your Breaddy account")
                .html(htmlContent)
                .build();

        mailgunMessagesApi.sendMessage(domain, message);
    }

    @Async
    public void sendApplicationStatusEmail(String email, ApplicationStatus status, Job job) {
        String subject;
        String text;

        if (status == ApplicationStatus.ACCEPTED) {
            subject = "Congratulations! Your application was accepted 🎉";
            text = String.format(
                    "Great news! Your application for '%s' at '%s' has been accepted.\nThe employer will contact you soon.",
                    job.getTitle(), job.getEmployer().getCompanyName()
            );
        } else if (status == ApplicationStatus.REJECTED) {
            subject = "Your application was not successful";
            text = String.format(
                    "Unfortunately your application for '%s' at '%s' has been rejected.\nKeep applying and good luck!",
                    job.getTitle(), job.getEmployer().getCompanyName()
            );
        } else {
            subject = "Your application status has changed";
            text = String.format(
                    "Your application for '%s' at '%s' has been updated to: %s",
                    job.getTitle(), job.getEmployer().getCompanyName(), status
            );
        }

        MailgunMessagesApi mailgunMessagesApi = MailgunClient.config(apiKey)
                .createApi(MailgunMessagesApi.class);

        Message message = Message.builder()
                .from("Breaddy <noreply@mail.breaddy.store>")
                .to(email)
                .subject(subject)
                .text(text)
                .build();

        mailgunMessagesApi.sendMessage(domain, message);
    }
}