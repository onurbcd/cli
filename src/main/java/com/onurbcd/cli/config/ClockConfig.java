package com.onurbcd.cli.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ClockConfig {

    /**
     * The single source of "now" for the application, so the time zone is explicit and tests can replace the clock.
     */
    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.systemDefault());
    }
}
