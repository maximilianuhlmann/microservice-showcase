package com.microservice.billing.config;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Feature flags service wrapper.
 * Provides a clean interface to check feature flags using Togglz.
 * 
 * @deprecated Use Features enum directly with Features.USAGE_AGGREGATION.isActive()
 * This class is kept for backward compatibility.
 */
@Component
@RequiredArgsConstructor
@Deprecated
public class FeatureFlags {
    
    /**
     * Check if real-time billing is enabled
     */
    public boolean isRealtimeBillingEnabled() {
        return Features.REALTIME_BILLING.isActive();
    }
    
    /**
     * Check if usage aggregation is enabled
     */
    public boolean isUsageAggregationEnabled() {
        return Features.USAGE_AGGREGATION.isActive();
    }
    
    /**
     * Check if invoice generation is enabled
     */
    public boolean isInvoiceGenerationEnabled() {
        return Features.INVOICE_GENERATION.isActive();
    }
    
    /**
     * Check if webhook notifications are enabled
     */
    public boolean isWebhookNotificationsEnabled() {
        return Features.WEBHOOK_NOTIFICATIONS.isActive();
    }
    
    /**
     * Check if advanced metrics are enabled
     */
    public boolean isAdvancedMetricsEnabled() {
        return Features.ADVANCED_METRICS.isActive();
    }
}

