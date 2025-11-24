package com.microservice.billing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;

/**
 * Togglz configuration for feature flags.
 * Uses in-memory state repository for MVP. Can be extended to use database or external service.
 */
@Configuration
public class TogglzConfig {

    @Bean
    public FeatureManager featureManager() {
        StateRepository stateRepository = new InMemoryStateRepository();
        
        // Initialize feature states from properties
        TogglzProperties properties = togglzProperties();
        
        return new FeatureManagerBuilder()
                .featureEnums(Features.class)
                .stateRepository(stateRepository)
                .userProvider(new UserProvider() {
                    @Override
                    public FeatureUser getCurrentUser() {
                        return new SimpleFeatureUser("system", true);
                    }
                })
                .build();
    }

    @Bean
    @ConfigurationProperties(prefix = "togglz")
    public TogglzProperties togglzProperties() {
        return new TogglzProperties();
    }

    public static class TogglzProperties {
        private boolean realtimeBilling = false;
        private boolean usageAggregation = true;
        private boolean invoiceGeneration = false;
        private boolean webhookNotifications = false;
        private boolean advancedMetrics = false;

        // Getters and setters
        public boolean isRealtimeBilling() { return realtimeBilling; }
        public void setRealtimeBilling(boolean realtimeBilling) { this.realtimeBilling = realtimeBilling; }
        
        public boolean isUsageAggregation() { return usageAggregation; }
        public void setUsageAggregation(boolean usageAggregation) { this.usageAggregation = usageAggregation; }
        
        public boolean isInvoiceGeneration() { return invoiceGeneration; }
        public void setInvoiceGeneration(boolean invoiceGeneration) { this.invoiceGeneration = invoiceGeneration; }
        
        public boolean isWebhookNotifications() { return webhookNotifications; }
        public void setWebhookNotifications(boolean webhookNotifications) { this.webhookNotifications = webhookNotifications; }
        
        public boolean isAdvancedMetrics() { return advancedMetrics; }
        public void setAdvancedMetrics(boolean advancedMetrics) { this.advancedMetrics = advancedMetrics; }
    }
}

