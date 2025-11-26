package com.microservice.billing.exception;

public class CustomerAccessDeniedException extends RuntimeException {

    private final String authenticatedCustomerId;
    private final String requestedCustomerId;

    public CustomerAccessDeniedException(String authenticatedCustomerId, String requestedCustomerId) {
        super("Access denied: You can only access your own data");
        this.authenticatedCustomerId = authenticatedCustomerId;
        this.requestedCustomerId = requestedCustomerId;
    }

    public CustomerAccessDeniedException(String message) {
        super(message);
        this.authenticatedCustomerId = null;
        this.requestedCustomerId = null;
    }

    public String getAuthenticatedCustomerId() {
        return authenticatedCustomerId;
    }

    public String getRequestedCustomerId() {
        return requestedCustomerId;
    }
}

