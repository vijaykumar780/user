package com.user.user.configs;

import com.google.gson.Gson;
import com.user.user.service.InterceptMethod;
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

    public MethodLoggingAspect(Gson gson,
                               KafkaTemplate<String, String> kafkaTemplate) {
        this.gson = gson;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Around("@annotation(interceptMethod)")
    public Object logMethodExecution(ProceedingJoinPoint originalMethod,
                                     InterceptMethod interceptMethod) throws Throwable {
        long start = System.currentTimeMillis();

        MethodSignature signature = (MethodSignature) originalMethod.getSignature();
        String methodName = signature.getDeclaringType().getSimpleName() + "." + signature.getName();

        Object[] args = originalMethod.getArgs();
        String[] paramNames = signature.getParameterNames();

        Map<String, Object> inputMap = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            inputMap.put(paramNames[i], args[i]);
        }

        Object result = null;
        try {
            result = originalMethod.proceed();
        } finally {
            long duration = System.currentTimeMillis() - start;

            Map<String, Object> logEvent = new HashMap<>();
            logEvent.put("method", methodName);
            logEvent.put("label", interceptMethod.value());
            logEvent.put("input", inputMap);
            logEvent.put("durationMs", duration);
            logEvent.put("timestamp", System.currentTimeMillis());
            logEvent.put("output", result);
            logEvent.put("correlationId", MDC.get("correlationId")); // Optional

            String json = gson.toJson(logEvent);
            log.info("Method execution: {}", json);

            // Optional: send to Kafka
            //kafkaTemplate.send("logs", json);
        }

        return result;
    }
}