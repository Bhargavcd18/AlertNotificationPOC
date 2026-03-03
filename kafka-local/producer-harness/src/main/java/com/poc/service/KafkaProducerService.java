package com.poc.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String TOPIC = "notifications.events";

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEvent(String fileName) throws Exception {

        InputStream inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream("payloads/" + fileName);

        String payload = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        JsonNode node = objectMapper.readTree(payload);
        String businessKey = node.get("businessKey").asText();

        kafkaTemplate.send(TOPIC, businessKey, payload);

        System.out.println("Sent: " + fileName + " | Key: " + businessKey);
    }
}