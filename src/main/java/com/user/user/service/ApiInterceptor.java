package com.user.user.service;

import com.google.gson.Gson;
import com.user.user.Constants;
import com.user.user.model.Event;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
@Slf4j
public class ApiInterceptor implements HandlerInterceptor {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private Gson gson;

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {
        long start = System.currentTimeMillis();
        request.setAttribute("apiStartTime", start);
        MDC.put(Constants.startTime, String.valueOf(start));
        MDC.put(Constants.correlationId, UUID.randomUUID().toString());

        // you can log or store headers, URI, query params etc.
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            org.springframework.web.servlet.ModelAndView modelAndView) {

    }

    @Override
    public void afterCompletion(HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) {
        long start   = Long.parseLong(MDC.get(Constants.startTime));
        long elapsed = System.currentTimeMillis() - start;
        String path  = request.getRequestURI();
        int status   = response.getStatus();

        Event event = Event.builder()
                .id(MDC.get(Constants.correlationId))
                .latency(elapsed)
                .status(status)
                .path(path)
                .timestamp(start)
                .userId(request.getParameter("userId"))
                .build();

        kafkaTemplate.send("logs", gson.toJson(event));
        log.info("Published event to kafka: {}", event);
        MDC.clear();
    }
}