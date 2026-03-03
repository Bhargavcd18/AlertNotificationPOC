package com.poc;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.serialization.StringSerializer;

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

        // Send 5 event types
        sendEvent(producer, "5001", "customer_created", "sms", "customer_created.json");
        sendEvent(producer, "5002", "account_created", "email", "account_created.json");
        sendEvent(producer, "5003", "loan_disbursed", "sms", "loan_disbursed.json");
        sendEvent(producer, "5004", "past_due", "email", "past_due.json");
        sendEvent(producer, "5005", "tph_transfer", "sms", "tph_transfer.json");

        producer.flush();
        producer.close();

        System.out.println("All messages sent successfully.");
    }

    private static void sendEvent(KafkaProducer<String, String> producer,
                                  String customerId,
                                  String eventType,
                                  String alertType,
                                  String fileName) throws Exception {

        String payload = loadPayload(fileName, customerId);

        ProducerRecord<String, String> record =
                new ProducerRecord<>(TOPIC, customerId, payload);

        // Add headers
        record.headers().add(new RecordHeader("event-type", eventType.getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader("alert-type", alertType.getBytes(StandardCharsets.UTF_8)));

        producer.send(record, (metadata, exception) -> {
            if (exception == null) {
                System.out.println("Sent to partition: " + metadata.partition()
                        + " offset: " + metadata.offset());
            } else {
                exception.printStackTrace();
            }
        });
    }

    private static String loadPayload(String fileName, String customerId) throws Exception {

        InputStream inputStream = ProducerHarness.class
                .getClassLoader()
                .getResourceAsStream("payloads/" + fileName);

        if (inputStream == null) {
            throw new RuntimeException("Payload file not found: " + fileName);
        }

        String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
System.out.println("Hi Manas");
        return content
                .replace("${customerId}", customerId)
                .replace("${timestamp}", String.valueOf(System.currentTimeMillis()));

    }
}