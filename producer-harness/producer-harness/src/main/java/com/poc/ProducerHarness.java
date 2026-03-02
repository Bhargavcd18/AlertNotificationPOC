package com.poc;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class ProducerHarness {

    private static final String TOPIC = "notifications.events";

    public static void main(String[] args) throws Exception {

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Production-style safety configs
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        // Send all frozen payloads
        sendEvent(producer, "account_created.json");
        sendEvent(producer, "customer_created.json");
        sendEvent(producer, "loan_disbursed.json");
        sendEvent(producer, "past_due.json");
        sendEvent(producer, "tph_transfer.json");

        producer.flush();
        producer.close();

        System.out.println("All messages sent successfully.");
    }

    private static void sendEvent(KafkaProducer<String, String> producer,
                                  String fileName) throws Exception {

        String payload = loadPayload(fileName);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(payload);

        // Extract businessKey for stable partition routing
        String businessKey = jsonNode.get("businessKey").asText();

        ProducerRecord<String, String> record =
                new ProducerRecord<>(TOPIC, businessKey, payload);

        producer.send(record, (metadata, exception) -> {
            if (exception == null) {
                System.out.println("File      : " + fileName);
                System.out.println("Key       : " + businessKey);
                System.out.println("Partition : " + metadata.partition());
                System.out.println("Offset    : " + metadata.offset());
                System.out.println("--------------------------------------");
            } else {
                exception.printStackTrace();
            }
        });
    }

    private static String loadPayload(String fileName) throws Exception {

        InputStream inputStream = ProducerHarness.class
                .getClassLoader()
                .getResourceAsStream("payloads/" + fileName);

        if (inputStream == null) {
            throw new RuntimeException("Payload file not found: " + fileName);
        }

        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
}