Absolutely, Vijay! Here's the complete guide with all steps as before — and in **Step 3**, the full aspect code is provided in a single file for clarity and ease of use.

---

## 🧩 Step 1: Add Spring AOP Dependency

### 🔧 Why?
Spring AOP allows you to intercept method calls and inject behavior like logging, timing, or Kafka publishing — without modifying the method itself.

### 📦 How?
Add this to your `pom.xml`:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

---

## 🧩 Step 2: Create a Custom Annotation

### 🔧 Why?
You want to selectively apply logging to specific methods. A custom annotation like `@LogExecution` gives you fine-grained control.

### 📦 How?

```java
package com.example.logging;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LogExecution {
    String value() default ""; // Optional label
}
```

---

## 🧩 Step 3: Create the Aspect (Full Code in One File)

### 🔧 Why?
This aspect intercepts methods annotated with `@LogExecution`, captures input/output, measures execution time, and logs or sends the data to Kafka.

### 📦 How?

```java
package com.example.logging;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
public class MethodLoggingAspect {

    private final Gson gson;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public MethodLoggingAspect(Gson gson, KafkaTemplate<String, String> kafkaTemplate) {
        this.gson = gson;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Around("@annotation(logExecution)")
    public Object logMethodExecution(ProceedingJoinPoint pjp, LogExecution logExecution) throws Throwable {
        long start = System.currentTimeMillis();

        MethodSignature signature = (MethodSignature) pjp.getSignature();
        String methodName = signature.getDeclaringType().getSimpleName() + "." + signature.getName();

        Object[] args = pjp.getArgs();
        String[] paramNames = signature.getParameterNames();

        Map<String, Object> inputMap = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            inputMap.put(paramNames[i], args[i]);
        }

        Object result;
        try {
            result = pjp.proceed();
        } finally {
            long duration = System.currentTimeMillis() - start;

            Map<String, Object> logEvent = new HashMap<>();
            logEvent.put("method", methodName);
            logEvent.put("label", logExecution.value());
            logEvent.put("input", inputMap);
            logEvent.put("durationMs", duration);
            logEvent.put("timestamp", System.currentTimeMillis());
            logEvent.put("output", result);
            logEvent.put("correlationId", MDC.get("correlationId")); // Optional

            String json = gson.toJson(logEvent);
            log.info("Method execution: {}", json);

            // Optional: send to Kafka
            kafkaTemplate.send("method-logs", json);
        }

        return result;
    }
}
```

---

## 🧩 Step 4: Annotate Your Methods

### 🔧 Why?
You now have a reusable annotation. Just add it to any method you want to monitor.

### 📦 How?

```java
package com.example.service;

import com.example.logging.LogExecution;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @LogExecution("fetch-user")
    public String getUserDetails(String userId) {
        return "User-" + userId;
    }
}
```

This method will now be intercepted. Input (`userId`), output (`User-123`), and execution time will be logged and optionally sent to Kafka.

---

## ✅ Summary

| Step | Purpose | Outcome |
|------|---------|---------|
| 1. Add AOP dependency | Enable method interception | Spring AOP is available |
| 2. Create annotation | Mark methods for logging | `@LogExecution` is reusable |
| 3. Build aspect | Intercept and log method calls | Input/output/timing captured |
| 4. Annotate methods | Apply logging selectively | Logs appear for tagged methods |

---

Would you like to auto-apply this to all public methods in a package (without annotations), or include exception logging and retry logic for Kafka publishing?
