package com.paradoxdevs.dollar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AppConfig {

    @Bean
    public Clock clock() {
        // This provides the real, system clock tied to the server's default timezone
        return Clock.systemDefaultZone();
    }
}
