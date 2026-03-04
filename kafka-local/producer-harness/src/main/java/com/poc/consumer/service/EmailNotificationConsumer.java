package com.poc.consumer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailNotificationConsumer {

    @KafkaListener(
            topics = "notifications.email",
            groupId = "notification-cg-email"
    )
    public void consumeEmail(String message) {

        log.info("📧 EMAIL CONSUMER GROUP (notification-cg-email) received message");
        log.info("Email Notification Payload: {}", message);

    }
}