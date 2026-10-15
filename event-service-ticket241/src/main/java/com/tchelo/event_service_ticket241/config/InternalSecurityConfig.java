package com.tchelo.event_service_ticket241.config;

import com.tchelo.event_service_ticket241.security.InternalApiKeyFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InternalSecurityConfig {

    // INTERNAL_API_KEY : IDENTIQUE dans auth-service, event-service, ticket-service et notification-service
    @Value("${event.internal.api-key}")
    private String internalApiKey;

    // Filtre appliqué uniquement aux endpoints internes, pas à l'API publique /api/events/**
    @Bean
    public FilterRegistrationBean<InternalApiKeyFilter> internalApiKeyFilterRegistration() {
        FilterRegistrationBean<InternalApiKeyFilter> registration =
                new FilterRegistrationBean<>(new InternalApiKeyFilter(internalApiKey));
        registration.addUrlPatterns("/internal/*");
        registration.setOrder(1);
        registration.setName("internalApiKeyFilter");
        return registration;
    }
}
