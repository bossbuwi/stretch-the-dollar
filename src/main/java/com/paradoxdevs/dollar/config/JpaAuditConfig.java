package com.paradoxdevs.dollar.config;

import com.paradoxdevs.dollar.entity.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditConfig {

    @Bean
    public AuditorAware<UUID> auditorProvider() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return Optional.empty();
            }

            Object principal = auth.getPrincipal();

            if (principal instanceof User user) {
                return Optional.of(user.getUuid());
            }

            if (principal instanceof Map<?,?> map && map.containsKey("uuid")) {
                String uuidStr = (String) map.get("uuid");
                return Optional.of(UUID.fromString(uuidStr));
            }
            return Optional.empty();
        };
    }
}
