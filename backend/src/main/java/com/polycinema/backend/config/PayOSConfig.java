package com.polycinema.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;

/**
 * Registers the PayOS SDK client as a Spring bean.
 * Credentials are read from environment variables / application.properties.
 *
 * SDK: vn.payos:payos-java:2.0.1
 * Constructor: PayOS(clientId, apiKey, checksumKey)
 */
@Configuration
public class PayOSConfig {

    @Value("${payos.client-id}")
    private String clientId;

    @Value("${payos.api-key}")
    private String apiKey;

    @Value("${payos.checksum-key}")
    private String checksumKey;

    @Bean
    public PayOS payOS() {
        return new PayOS(clientId, apiKey, checksumKey);
    }
}
