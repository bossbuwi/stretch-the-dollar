package com.paradoxdevs.dollar.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.info("Health check ==> alive");
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body("Hello World!");
    }
}
