package com.user.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
public class Controller {

    private final MeterRegistry meterRegistry;

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public Controller(MeterRegistry meterRegistry,
                      KafkaTemplate<String, String> kafkaTemplate) {
        this.meterRegistry = meterRegistry;
        this.kafkaTemplate = kafkaTemplate;
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
    public ResponseEntity<String> user(@RequestParam String userId) {
        meterRegistry.counter("api.users.count", "status", "success").increment();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", userId);
        jsonObject.put("email", System.currentTimeMillis()+"@gmail.com");
        jsonObject.put("timestamp", System.currentTimeMillis());
        jsonObject.put("id", UUID.randomUUID());
        try {
            // kafka should be working
            //kafkaTemplate.send("logs", String.valueOf(jsonObject));
        } catch (Exception e) {
            log.error("Error sending user data to Kafka", e);
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonObject.toString());
    }
}
