package com.microservice.billing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.spring.security.SpringSecurityUserProvider;

@Configuration
public class TogglzConfig {

    @Bean
    public FeatureManager featureManager(final TogglzProperties togglzProperties) {
        final InMemoryStateRepository stateRepository = new InMemoryStateRepository();
        
        setFeatureState(stateRepository, Features.REALTIME_BILLING, togglzProperties.isRealtimeBilling());
        setFeatureState(stateRepository, Features.USAGE_AGGREGATION, togglzProperties.isUsageAggregation());
        setFeatureState(stateRepository, Features.INVOICE_GENERATION, togglzProperties.isInvoiceGeneration());
        setFeatureState(stateRepository, Features.WEBHOOK_NOTIFICATIONS, togglzProperties.isWebhookNotifications());
        setFeatureState(stateRepository, Features.ADVANCED_METRICS, togglzProperties.isAdvancedMetrics());
        
        return new FeatureManagerBuilder()
                .featureEnum(Features.class)
                .stateRepository(stateRepository)
                .userProvider(new SpringSecurityUserProvider("ROLE_ADMIN"))
                .build();
    }
    
    private void setFeatureState(final InMemoryStateRepository repository, final Features feature, final boolean enabled) {
        final FeatureState state = new FeatureState(feature, enabled);
        repository.setFeatureState(state);
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
