package com.microservice.billing;

import com.microservice.billing.config.PostgresTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(properties = {
    "spring.main.allow-bean-definition-overriding=true",
    "spring.main.lazy-initialization=true"
})
@ActiveProfiles("test")
@ContextConfiguration(initializers = PostgresTestContainer.class)
class UsageBillingAppTests {

    @Test
    void contextLoads() {
        // Verifies that the Spring application context loads successfully
    }
}

