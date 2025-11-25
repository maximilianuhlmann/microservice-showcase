package com.microservice.billing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;

@Configuration
public class TogglzConfig {

    @Bean
    public FeatureManager featureManager() {
        final StateRepository stateRepository = new InMemoryStateRepository();
        
        return new FeatureManagerBuilder()
                .featureEnum(Features.class)
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
        private boolean realtimeBilling;
        private boolean usageAggregation = true;
        private boolean invoiceGeneration;
        private boolean webhookNotifications;
        private boolean advancedMetrics;

        public boolean isRealtimeBilling() { return realtimeBilling; }
        public void setRealtimeBilling(final boolean realtimeBilling) { this.realtimeBilling = realtimeBilling; }
        
        public boolean isUsageAggregation() { return usageAggregation; }
        public void setUsageAggregation(final boolean usageAggregation) { this.usageAggregation = usageAggregation; }
        
        public boolean isInvoiceGeneration() { return invoiceGeneration; }
        public void setInvoiceGeneration(final boolean invoiceGeneration) { this.invoiceGeneration = invoiceGeneration; }
        
        public boolean isWebhookNotifications() { return webhookNotifications; }
        public void setWebhookNotifications(final boolean webhookNotifications) { this.webhookNotifications = webhookNotifications; }
        
        public boolean isAdvancedMetrics() { return advancedMetrics; }
        public void setAdvancedMetrics(final boolean advancedMetrics) { this.advancedMetrics = advancedMetrics; }
    }
}

