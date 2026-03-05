package com.poc.consumer.validation;

import com.fasterxml.jackson.databind.JsonNode;

public class ConsumerValidator {

    public static void validate(JsonNode customFields) {

        validateCustomerId(customFields.path("customer_id").asText());
        validateCustomerName(customFields.path("customer_name").asText());

        validateNotEmpty("timestamp", customFields.path("timestamp").asText());
        validateNotEmpty("amount", customFields.path("amount").asText());
        validateNotEmpty("loan_account", customFields.path("loan_account").asText());

        validateTxnRef(customFields.path("txn_ref").asText());

        validateNotEmpty("days_overdue", customFields.path("days_overdue").asText());
        validateNotEmpty("min_amount_due", customFields.path("min_amount_due").asText());
        validateNotEmpty("grace_date", customFields.path("grace_date").asText());

        validateLoanRef(customFields.path("loan_ref").asText());

        validateNotEmpty("beneficiary_name", customFields.path("beneficiary_name").asText());

        validateBeneficiaryId(customFields.path("beneficiary_id").asText());
    }

    // Generic method for null/empty validation
    private static void validateNotEmpty(String fieldName, String value) {

        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException(fieldName + " cannot be null or empty");
        }
    }

    // Specific validations

    private static void validateCustomerId(String value) {

        validateNotEmpty("customer_id", value);

        if (!value.matches("\\d{10}")) {
            throw new RuntimeException("customer_id must contain exactly 10 digits");
        }
    }

    private static void validateCustomerName(String value) {

        validateNotEmpty("customer_name", value);

        if (!value.matches("^[A-Za-z ]+$")) {
            throw new RuntimeException("customer_name must contain only alphabets");
        }
    }

    private static void validateTxnRef(String value) {

        validateNotEmpty("txn_ref", value);

        if (!value.matches("^[A-Za-z0-9]+$")) {
            throw new RuntimeException("txn_ref must contain alphabets and numbers only");
        }
    }

    private static void validateLoanRef(String value) {

        validateNotEmpty("loan_ref", value);

        if (!value.matches("^[A-Za-z0-9]+$")) {
            throw new RuntimeException("loan_ref must contain alphabets and numbers only");
        }
    }

    private static void validateBeneficiaryId(String value) {

        validateNotEmpty("beneficiary_id", value);

        if (!value.matches("^[A-Za-z0-9]+$")) {
            throw new RuntimeException("beneficiary_id must contain alphabets and numbers only");
        }
    }
}