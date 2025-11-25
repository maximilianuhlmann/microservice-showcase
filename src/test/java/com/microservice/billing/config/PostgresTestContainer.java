package com.microservice.billing.config;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class PostgresTestContainer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final PostgreSQLContainer<?> postgres;

    static {
        @SuppressWarnings("resource") // Container is closed via shutdown hook on JVM shutdown
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpass123");
        container.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (container != null && container.isRunning()) {
                    container.close();
                }
            } catch (Exception e) {
                // Ignore exceptions during shutdown
            }
        }));
        
        postgres = container;
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        TestPropertyValues.of(
                "spring.datasource.url=" + postgres.getJdbcUrl(),
                "spring.datasource.username=" + postgres.getUsername(),
                "spring.datasource.password=" + postgres.getPassword(),
                "spring.datasource.driver-class-name=" + postgres.getDriverClassName()
        ).applyTo(applicationContext.getEnvironment());
    }
}

