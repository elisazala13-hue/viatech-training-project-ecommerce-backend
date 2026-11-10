package com.ecommerce.services;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.password-reset-expiration-minutes}")
    private long expirationMinutes;

    @Async
    public void sendPasswordReset(String to, String link) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject("Reset your password");
            msg.setText("""
                    We received a request to reset your password.

                    Click the link below to choose a new one (valid for %d minutes):
                    %s

                    If you didn't request this, you can ignore this email.
                    """.formatted(expirationMinutes, link));
            mailSender.send(msg);
        } catch (MailException e) {
            log.error("Failed to send password reset email", e);
        }
    }
}
