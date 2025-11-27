package com.microservice.billing.exception;

import java.io.Serial;

public class DomainValidationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String fieldName;
    private final transient Object invalidValue;

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

