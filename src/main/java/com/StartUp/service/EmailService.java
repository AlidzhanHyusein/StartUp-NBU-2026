package com.StartUp.service;

import com.StartUp.entity.Job;
import com.StartUp.enums.ApplicationStatus;
import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${resend.api-key}")
    private String resendApiKey;

    @Value("${app.base-url}")
    private String baseUrl;

    @Async
    public void sendVerificationEmail(String toEmail, String token) {
        String link = baseUrl + "/api/auth/verify?token=" + token;

        try {
            Resend resend = new Resend(resendApiKey);
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from("onboarding@resend.dev")
                    .to(toEmail)
                    .subject("Verify your email")
                    .html("<h2>Verify your account</h2>" +
                            "<p>Click the link below to verify your account:</p>" +
                            "<a href='" + link + "'>Verify Email</a>")
                    .build();
            resend.emails().send(params);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send verification email: " + e.getMessage());
        }
    }

    @Async
    public void sendApplicationStatusEmail(String email, ApplicationStatus status, Job job) {
        String subject;
        String text;

        if (status == ApplicationStatus.ACCEPTED) {
            subject = "Congratulations! Your application was accepted 🎉";
            text = String.format(
                    "<h2>Great news!</h2><p>Your application for the position <b>%s</b> at <b>%s</b> has been accepted.</p><p>The employer will contact you soon.</p>",
                    job.getTitle(), job.getEmployer().getCompanyName()
            );
        } else if (status == ApplicationStatus.REJECTED) {
            subject = "Your application was not successful";
            text = String.format(
                    "<h2>Application Update</h2><p>Unfortunately your application for the position <b>%s</b> at <b>%s</b> has been rejected.</p><p>Keep applying and good luck!</p>",
                    job.getTitle(), job.getEmployer().getCompanyName()
            );
        } else {
            subject = "Your application status has changed";
            text = String.format(
                    "<h2>Application Update</h2><p>Your application for the position <b>%s</b> at <b>%s</b> has been updated to: <b>%s</b></p>",
                    job.getTitle(), job.getEmployer().getCompanyName(), status
            );
        }

        try {
            Resend resend = new Resend(resendApiKey);
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from("onboarding@resend.dev")
                    .to(email)
                    .subject(subject)
                    .html(text)
                    .build();
            resend.emails().send(params);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send status email: " + e.getMessage());
        }
    }
}