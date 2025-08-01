package com.user.user.schedulers;

import com.user.user.service.InterceptMethod;
import com.user.user.service.SchedulerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class Scheduler {

    @Autowired
    private SchedulerService schedulerService;

    @Scheduled(fixedDelay = 10000)// 10 seconds
    public void runScheduler() {
        schedulerService.runScheduler("val1", "val2");

    }
}
