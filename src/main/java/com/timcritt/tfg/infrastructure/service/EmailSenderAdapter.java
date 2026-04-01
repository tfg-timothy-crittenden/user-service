package com.timcritt.tfg.infrastructure.service;

import com.timcritt.tfg.application.port.outbound.EmailSenderPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailSenderAdapter implements EmailSenderPort {

    private final JavaMailSender mailSender;
    private final String from;

    public EmailSenderAdapter(JavaMailSender mailSender, @Value("${app.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void sendVerificationEmail(String to, String verificationLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Confirm your email");
        message.setText("Click this to confirm your email: " + verificationLink);
        mailSender.send(message);
    }

    @Override
    public void sendInvitationEmail(String to, String invitationLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Invitation to join the platform");
        message.setText("Click this to join the platform: " + invitationLink);
        mailSender.send(message);
    }

    @Override
    public void sendPasswordResetEmail(String to, String passwordResetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Password reset");
        message.setText("Click this to reset your password: " + passwordResetLink);
        mailSender.send(message);
    }
}
