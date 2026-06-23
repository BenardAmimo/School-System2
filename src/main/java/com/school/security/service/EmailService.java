package com.school.security.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender javaMailSender;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }
    public void sendEmailToUser(String to , String from ,String code){
        SimpleMailMessage messaging = new SimpleMailMessage();
        messaging.setTo(to);
        messaging.setFrom(from);
        messaging.setSubject("Verification code to your account");
        messaging.setText("Use the code sent below to verify your account creation:\n"+ code);

        javaMailSender.send(messaging);

    }
}
