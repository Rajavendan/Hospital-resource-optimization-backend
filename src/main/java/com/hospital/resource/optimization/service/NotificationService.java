package com.hospital.resource.optimization.service;

public interface NotificationService {
    void sendSms(String phoneNumber, String message);

    void sendEmail(String to, String subject, String body);

    void sendWhatsApp(String phoneNumber, String message);
}
