package com.microservice.billing.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${admin.username:admin}")
    private String adminUsername;

    @Value("${admin.password:admin123}")
    private String adminPassword;

    private final ApiKeyAuthenticationProvider apiKeyAuthenticationProvider;
    private final ApiKeyValidator apiKeyValidator;
    private final AuthenticationConfiguration authenticationConfiguration;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        AuthenticationManager authenticationManager = authenticationConfiguration.getAuthenticationManager();
        
        // Create API key filter with authentication manager
        ApiKeyAuthenticationFilter apiKeyFilter = new ApiKeyAuthenticationFilter(authenticationManager, apiKeyValidator);
        
        http
            // Add API key filter for REST API authentication
            .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
            .authenticationProvider(apiKeyAuthenticationProvider)
            .authorizeHttpRequests(auth -> auth
                // API endpoints: authenticated via API key OR public (if no API keys configured)
                // The ApiKeyAuthenticationFilter handles validation
                .requestMatchers("/api/**").permitAll() // Filter handles authentication
                .requestMatchers("/swagger-ui/**", "/api-docs/**", "/v3/api-docs/**").permitAll()
                // Admin endpoints require basic authentication
                .requestMatchers("/togglz/**", "/h2-console/**").authenticated()
                // All other requests
                .anyRequest().permitAll()
            )
            .httpBasic(httpBasic -> {}) // Enable basic authentication for admin endpoints
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/h2-console/**") // Disable CSRF for REST API and H2 console
            )
            .headers(headers -> headers
                .frameOptions(org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig::sameOrigin) // Allow H2 console frames
            );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
            .username(adminUsername)
            .password(passwordEncoder().encode(adminPassword))
            .roles("ADMIN")
            .build();

        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

