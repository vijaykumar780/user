package com.user.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.user.user.model.AuthRequest;
import com.user.user.model.AuthResponse;
import com.user.user.model.User;
import com.user.user.service.DBService;
import com.user.user.service.JWTService;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
public class Controller {

    private final MeterRegistry meterRegistry;

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthenticationManager authenticationManager;
    private DBService dbService;
    private JWTService jwtService;

    public Controller(MeterRegistry meterRegistry,
                      KafkaTemplate<String, String> kafkaTemplate,
                      AuthenticationManager authenticationManager,
                      DBService dbService,
                      JWTService jwtService) {
        this.meterRegistry = meterRegistry;
        this.kafkaTemplate = kafkaTemplate;
        this.authenticationManager = authenticationManager;
        this.dbService = dbService;
        this.jwtService = jwtService;
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

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthRequest authRequest) throws Exception {
        // Authenticate using Spring Security
        /*authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );
*/
        // Load user details and generate JWT
        Optional<User> userDetails = dbService.getUser(authRequest.getUsername());

        // handle case where user is not found
        if (userDetails.isEmpty()) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
        // check if password matches
        // use Bcrypt or any other hashing mechanism in production
        if (!userDetails.get().getPassword().equals(authRequest.getPassword())) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
        String jwt = jwtService.generateToken(userDetails.get());

        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    // JWT applies on this endpoint
    @GetMapping("/getUserDetails")
    public ResponseEntity<?> getUserDetails(@RequestHeader ("Authorization") String auth) {
        // user details can be fetched from the security context
        String username = null;
        if (auth != null && auth.startsWith("Bearer ")) {
            username = jwtService.extractUsername(auth.substring(7));
        }
        log.info("Received user: {}", username);
        JSONObject userDetails = new JSONObject();
        userDetails.put("name", "Vijay");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(userDetails.toString());
    }
}
