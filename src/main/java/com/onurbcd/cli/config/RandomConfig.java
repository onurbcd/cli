package com.onurbcd.cli.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.SecureRandom;

@Configuration
public class RandomConfig {

    /**
     * The single source of cryptographically secure randomness (e.g. IVs), so tests can replace it.
     */
    @Bean
    public SecureRandom secureRandom() {
        return new SecureRandom();
    }
}
