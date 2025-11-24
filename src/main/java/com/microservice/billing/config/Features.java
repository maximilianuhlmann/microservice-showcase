package com.microservice.billing.config;

import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;
import org.togglz.core.context.FeatureContext;

/**
 * Feature flags enum using Togglz.
 * This allows toggling features on/off at runtime.
 */
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

    /**
     * Helper method to check if a feature is active.
     */
    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }
}

