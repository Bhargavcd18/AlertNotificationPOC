package com.poc.notification.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationConsumer {

    @KafkaListener(
            topics = "notifications.events",
            groupId = "notification-poc-group"
    )
    public void consume(
            @Payload String payload,
            @Headers MessageHeaders headers) {

        String eventType =
                new String((byte[]) headers.get("event-type"));

        String alertType =
                new String((byte[]) headers.get("alert-type"));

        log.info("================================");
        log.info("EVENT RECEIVED");
        log.info("Event Type : {}", eventType);
        log.info("Alert Type : {}", alertType);
        log.info("Payload    : {}", payload);
        log.info("test");
        System.out.print("Hi Bharo");

        if ("sms".equalsIgnoreCase(alertType)) {
            log.info("✅ SMS Notification Processed");
        } else if ("email".equalsIgnoreCase(alertType)) {
            log.info("✅ Email Notification Processed");
        }

        log.info("================================");
    }
}