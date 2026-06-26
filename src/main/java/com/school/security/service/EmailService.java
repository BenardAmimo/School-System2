package com.school.security.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender javaMailSender;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }
    @Value("${spring.mail.username}")
    private String fromEmail;
    public void sendEmailToUser(String to,String code){
        SimpleMailMessage messaging = new SimpleMailMessage();
        messaging.setTo(to);
        messaging.setFrom(fromEmail);
        messaging.setSubject("Verification code to your account");
        messaging.setText("Use the code sent below to verify your account creation:\n"+ code);

        javaMailSender.send(messaging);

    }
}
