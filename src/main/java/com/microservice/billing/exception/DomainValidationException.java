package com.microservice.billing.exception;

public class DomainValidationException extends RuntimeException {

    private final String fieldName;
    private final Object invalidValue;

    public DomainValidationException(String fieldName, Object invalidValue, String message) {
        super(message);
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
    }

    public DomainValidationException(String message) {
        super(message);
        this.fieldName = null;
        this.invalidValue = null;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getInvalidValue() {
        return invalidValue;
    }
}

