package com.hospital.resource.optimization.service.impl;

import com.hospital.resource.optimization.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final JavaMailSender javaMailSender;

    @Value("${fast2sms.api.key}")
    private String smsApiKey;

    private final com.hospital.resource.optimization.service.TwilioService twilioService;

    public NotificationServiceImpl(JavaMailSender javaMailSender,
            com.hospital.resource.optimization.service.TwilioService twilioService) {
        this.javaMailSender = javaMailSender;
        this.twilioService = twilioService;
    }

    @Override
    public void sendSms(String phoneNumber, String message) {
        // Use Twilio Service
        twilioService.sendSms(phoneNumber, message);
    }

    @Async
    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(to);
            mailMessage.setSubject(subject);
            mailMessage.setText(body);
            javaMailSender.send(mailMessage);
            System.out.println("Email sent successfully to " + to + " with subject: " + subject);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    @Override
    public void sendWhatsApp(String phoneNumber, String message) {
        System.out.println("DTO: Sending WhatsApp to " + phoneNumber + ": " + message);
    }
}
