package com.poc.runner;

import com.poc.service.KafkaProducerService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements CommandLineRunner {

    private final KafkaProducerService producerService;

    public StartupRunner(KafkaProducerService producerService) {
        this.producerService = producerService;
    }

    @Override
    public void run(String... args) throws Exception {

        producerService.sendEvent("account_created.json");
        producerService.sendEvent("customer_created.json");
        producerService.sendEvent("loan_disbursed.json");
        producerService.sendEvent("past_due.json");
        producerService.sendEvent("tph_transfer.json");

        System.out.println("All events sent.");
    }
}