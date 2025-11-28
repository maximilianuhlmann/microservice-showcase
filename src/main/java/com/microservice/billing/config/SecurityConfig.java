package com.microservice.billing.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
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
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    @Value("${admin.username:admin}")
    private String adminUsername;

    @Value("${admin.password:admin123}")
    private String adminPassword;

    private final ApiKeyAuthenticationProvider apiKeyAuthenticationProvider;
    private final ApiKeyValidator apiKeyValidator;

    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        return new org.springframework.security.authentication.ProviderManager(
            apiKeyAuthenticationProvider,
            daoAuthenticationProvider(userDetailsService, passwordEncoder)
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) throws Exception {
        
        ApiKeyAuthenticationFilter apiKeyFilter = new ApiKeyAuthenticationFilter(authenticationManager, apiKeyValidator);
        
        http
            .authenticationProvider(apiKeyAuthenticationProvider)
            .authenticationProvider(daoAuthenticationProvider(userDetailsService, passwordEncoder))
            .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/api-docs/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/togglz-console/**").authenticated()
                .requestMatchers("/h2-console/**").authenticated()
                .anyRequest().permitAll()
            )
            .httpBasic(httpBasic -> {
                BasicAuthenticationEntryPoint entryPoint = new BasicAuthenticationEntryPoint();
                entryPoint.setRealmName("Togglz Console");
                httpBasic.authenticationEntryPoint(entryPoint);
            })
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/h2-console/**", "/togglz-console/**")
            )
            .headers(headers -> headers
                .frameOptions(org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig::sameOrigin)
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        String encodedPassword = passwordEncoder.encode(adminPassword);
        
        UserDetails admin = User.builder()
            .username(adminUsername)
            .password(encodedPassword)
            .roles("ADMIN")
            .build();

        return new InMemoryUserDetailsManager(admin);
    }
}

