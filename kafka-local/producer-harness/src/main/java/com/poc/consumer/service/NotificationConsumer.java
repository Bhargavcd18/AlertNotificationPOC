package com.poc.consumer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
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

    // MAIN LISTENER WITH RETRY
    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 5000),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(
            topics = "notifications.events",
            groupId = "notification-poc-group"
    )
    public void consume(String payload) {

        try {

            log.info("Message received from main topic");

            // Store RAW message before enrichment
            storeRawMessage(payload);

            // Enrich JSON
            ObjectNode rootNode =
                    (ObjectNode) objectMapper.readTree(payload);

            String businessKey =
                    rootNode.path("businessKey").asText();

            rootNode.put("canonicalKey_SMS", businessKey + "|SMS");
            rootNode.put("canonicalKey_Email", businessKey + "|EMAIL");

            String enrichedMessage =
                    objectMapper.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(rootNode);

            // Log enriched message
            log.info("Enriched Message:\n{}", enrichedMessage);

            // Store enriched message in file
            storeEnrichedMessage(enrichedMessage);

            log.info("Message processed successfully with enrichment.");

        } catch (Exception e) {

            log.error("Processing failed. Triggering retry...", e);

            // IMPORTANT: Throw exception to trigger retry
            throw new RuntimeException(e);
        }
    }

    // ===============================
    // DLT HANDLER (QUARANTINE ZONE)
    // ===============================
    @DltHandler
    public void handleDlt(String payload) {

        try {

            log.error("Message moved to DLT after all retries exhausted.");

            String quarantineLog =
                    "================ DLT EVENT =================\n" +
                            "Moved At: " + LocalDateTime.now() + "\n" +
                            payload + "\n\n";

            Files.writeString(
                    Path.of("quarantine-events.log"),
                    quarantineLog,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

            log.error("Failed message stored in quarantine file.");

        } catch (Exception e) {
            log.error("Failed to store DLT message", e);
        }
    }

    // ===============================
    // RAW EVENT STORAGE
    // ===============================
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

        log.info("Raw event stored successfully.");
    }

    // ===============================
    // ENRICHED EVENT STORAGE
    // ===============================
    private void storeEnrichedMessage(String enrichedMessage) throws Exception {

        String logEntry =
                "================ ENRICHED EVENT =================\n" +
                        "Processed At: " + LocalDateTime.now() + "\n" +
                        enrichedMessage + "\n\n";

        Files.writeString(
                Path.of("enriched-events.log"),
                logEntry,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );

        log.info("Enriched event stored successfully.");
    }
}