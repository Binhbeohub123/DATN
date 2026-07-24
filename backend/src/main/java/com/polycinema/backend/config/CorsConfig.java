package com.polycinema.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Centralised CORS configuration.
 *
 * Replaces the @CrossOrigin annotation that was previously hardcoded on every
 * controller.  The allowed origin(s) are now controlled by the
 * CORS_ALLOWED_ORIGINS environment variable, so the same artifact works for
 * local dev, staging, and production without any code changes.
 *
 * Set in application.properties:
 *   cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:5173}
 *
 * For multiple origins supply a comma-separated list:
 *   CORS_ALLOWED_ORIGINS=https://yourapp.railway.app,https://www.yourapp.com
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
