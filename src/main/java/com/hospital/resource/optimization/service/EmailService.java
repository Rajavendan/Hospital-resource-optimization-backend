package com.hospital.resource.optimization.service;

public interface EmailService {
    void sendSimpleMessage(String to, String subject, String text);

    void sendHtmlMessage(String to, String subject, String htmlBody);
}
