package com.hospital.resource.optimization.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

@Service
public class TwilioService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.messaging.service.sid}")
    private String messagingServiceSid;

    @PostConstruct
    public void init() {
        // Twilio initialization is deferred to sendSms method to prevent startup failure if token is invalid
    }

    public void sendSms(String to, String body) {
        try {
            // Check if token is set (mock check)
            if (authToken == null || authToken.isEmpty() || "YOUR_AUTH_TOKEN".equals(authToken)) {
                System.out.println("Twilio Auth Token missing. SMS would be: " + body + " to " + to);
                return;
            }

            Twilio.init(accountSid, authToken);
            Message message = Message.creator(
                    new PhoneNumber(to),
                    messagingServiceSid,
                    body)
                    .create();
            System.out.println("SMS sent successfully: " + message.getSid());
        } catch (Exception e) {
            System.err.println("Error sending SMS: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
