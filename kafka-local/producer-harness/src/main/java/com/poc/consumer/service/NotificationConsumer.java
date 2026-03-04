package com.poc.consumer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

@Slf4j
@Service
public class NotificationConsumer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    // MAIN LISTENER WITH RETRY

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 5000),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(
            topics = "notifications.events",
            groupId = "notification-router-group"
    )
    public void consume(String payload) {

        try {

            log.info("📥 Router received message");

            // Store RAW producer message
            storeRawMessage(payload);

            ObjectNode rootNode =
                    (ObjectNode) objectMapper.readTree(payload);

            String businessKey =
                    rootNode.path("businessKey").asText();

            // Add enrichment fields
            rootNode.put("canonicalKey_SMS", businessKey + "|SMS");
            rootNode.put("canonicalKey_Email", businessKey + "|EMAIL");

            String enrichedMessage =
                    objectMapper.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(rootNode);

            // Log enriched message
            log.info("Enriched Message:\n{}", enrichedMessage);

            // Extract MessageType
            String messageType =
                    rootNode
                            .path("payload")
                            .path("customFieldDetails")
                            .path("MessageType")
                            .asText();

            log.info("MessageType detected: {}", messageType);

            // Routing logic
            if ("BOTH".equalsIgnoreCase(messageType)) {
                storeSmsMessage(enrichedMessage);
                storeEmailMessage(enrichedMessage);
                kafkaTemplate.send("notifications.sms", enrichedMessage);
                kafkaTemplate.send("notifications.email", enrichedMessage);

                log.info("➡ Routed to BOTH SMS and EMAIL topics");

            } else if ("SMS".equalsIgnoreCase(messageType)) {
                storeSmsMessage(enrichedMessage);
                kafkaTemplate.send("notifications.sms", enrichedMessage);

                log.info("➡ Routed to SMS topic only");

            } else if ("EMAIL".equalsIgnoreCase(messageType)) {
                storeEmailMessage(enrichedMessage);
                kafkaTemplate.send("notifications.email", enrichedMessage);

                log.info("➡ Routed to EMAIL topic only");

            } else {

                log.warn("Unknown MessageType: {}", messageType);
            }

        } catch (Exception e) {

            log.error("Processing failed, triggering retry", e);

            throw new RuntimeException(e);
        }
    }

    // DLT HANDLER

    @DltHandler
    public void handleDlt(String payload) {

        try {

            log.error("Message moved to DLT after retries exhausted");

            // Store failed message
            storeDltMessage(payload);

        } catch (Exception e) {

            log.error("Failed to store DLT message", e);
        }
    }

    // STORE RAW PRODUCER MESSAGE

    private void storeRawMessage(String payload) throws Exception {

        String logEntry =
                "================ RAW EVENT =================\n" +
                        "Received At: " + LocalDateTime.now() + "\n" +
                        payload + "\n\n";

        Files.writeString(
                Path.of("raw-events.log"),
                logEntry,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
        log.info("raw message stored in raw-events file successfully.");
    }

        private void storeSmsMessage(String payload) throws Exception {

            String logEntry =
                    "================ RAW EVENT =================\n" +
                            "Received At: " + LocalDateTime.now() + "\n" +
                            payload + "\n\n";

            Files.writeString(
                    Path.of("sms-events.log"),
                    logEntry,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

        log.info("producer message stored in SMS consumer group successfully.");
    }

        private void storeEmailMessage(String payload) throws Exception {

            String logEntry =
                    "================ RAW EVENT =================\n" +
                            "Received At: " + LocalDateTime.now() + "\n" +
                            payload + "\n\n";

            Files.writeString(
                    Path.of("email-events.log"),
                    logEntry,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

            log.info("producer message stored in EMAIL consumer group successfully.");
        }

    // STORE DLT MESSAGE

    private void storeDltMessage(String payload) throws Exception {

        String logEntry =
                "================ DLT EVENT =================\n" +
                        "Moved To DLT At: " + LocalDateTime.now() + "\n" +
                        payload + "\n\n";

        Files.writeString(
                Path.of("dlt-events.log"),
                logEntry,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );

        log.error("DLT message stored in dlt-events.log");
    }
}