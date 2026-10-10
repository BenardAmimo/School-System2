package com.school.security.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${MAIL_USERNAME}")
    private String fromMail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    public void sendInviteEmail(String to, String token) {

        String link = frontendUrl + "/setup-password#code=" + token;

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromMail);
        msg.setTo(to);
        msg.setSubject("Set up your school account");
        msg.setText("""
            Hello,

            An administrator has created an account for you.
            Use the link below to set your username and password:

            %s

            This link expires in 3 days and can only be used once.
            If you were not expecting this email, you can ignore it.
            """.formatted(link));
        mailSender.send(msg);
    }
}
