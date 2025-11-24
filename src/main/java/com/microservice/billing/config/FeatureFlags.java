package com.microservice.billing.config;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.togglz.core.manager.FeatureManager;

/**
 * Feature flags service wrapper.
 * Provides a clean interface to check feature flags using Togglz.
 * 
 * @deprecated Inject FeatureManager directly instead of using this wrapper.
 * This class is kept for backward compatibility.
 */
@Component
@RequiredArgsConstructor
@Deprecated
public class FeatureFlags {
    
    private final FeatureManager featureManager;
    
    /**
     * Check if real-time billing is enabled
     */
    public boolean isRealtimeBillingEnabled() {
        return featureManager.isActive(Features.REALTIME_BILLING);
    }
    
    /**
     * Check if usage aggregation is enabled
     */
    public boolean isUsageAggregationEnabled() {
        return featureManager.isActive(Features.USAGE_AGGREGATION);
    }
    
    /**
     * Check if invoice generation is enabled
     */
    public boolean isInvoiceGenerationEnabled() {
        return featureManager.isActive(Features.INVOICE_GENERATION);
    }
    
    /**
     * Check if webhook notifications are enabled
     */
    public boolean isWebhookNotificationsEnabled() {
        return featureManager.isActive(Features.WEBHOOK_NOTIFICATIONS);
    }
    
    /**
     * Check if advanced metrics are enabled
     */
    public boolean isAdvancedMetricsEnabled() {
        return featureManager.isActive(Features.ADVANCED_METRICS);
    }
}

