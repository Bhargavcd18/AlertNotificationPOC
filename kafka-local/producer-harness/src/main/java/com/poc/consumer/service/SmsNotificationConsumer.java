package com.poc.consumer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsNotificationConsumer {

    @KafkaListener(
            topics = "notifications.sms",
            groupId = "notification-cg-sms"
    )
    public void consumeSms(String message) {

        log.info("📱 SMS CONSUMER GROUP (notification-cg-sms) received message");
        log.info("SMS Notification Payload: {}", message);

    }
}