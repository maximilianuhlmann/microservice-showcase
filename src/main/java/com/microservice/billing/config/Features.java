package com.microservice.billing.config;

import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;

public enum Features implements Feature {

    @Label("Real-time Billing")
    REALTIME_BILLING,

    @Label("Usage Aggregation")
    USAGE_AGGREGATION,

    @Label("Invoice Generation")
    INVOICE_GENERATION,

    @Label("Webhook Notifications")
    WEBHOOK_NOTIFICATIONS,

    @Label("Advanced Metrics")
    ADVANCED_METRICS;
}

