package com.user.user;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class Controller {

    private final MeterRegistry meterRegistry;

    public Controller(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @GetMapping("/health")
    @Timed(value = "api.health.time", description = "Time to fetch health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        meterRegistry.counter("api.health.count", "status", "success").increment();

        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        log.info("Health check: {}", status);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/user")
    @Timed(value = "api.users.time", description = "Time to fetch users")
    public ResponseEntity<Map<String, String>> user() {
        meterRegistry.counter("api.users.count", "status", "success").increment();

        Map<String, String> user = new HashMap<>();
        user.put("name", "Aws1");
        user.put("email", "email");
        log.info("User: {}", user);
        return ResponseEntity.ok(user);
    }
}
