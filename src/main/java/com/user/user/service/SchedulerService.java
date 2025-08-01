package com.user.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SchedulerService {

    @InterceptMethod(value = "Scheduler")
    public String runScheduler(String arg1, String arg2) {
        log.info("SchedulerService is running with arguments: {}, {}", arg1, arg2);
        // Simulate some processing

        return arg1 + ":" + arg2;
    }
}
