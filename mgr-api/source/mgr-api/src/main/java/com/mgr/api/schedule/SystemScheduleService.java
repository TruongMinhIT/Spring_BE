package com.mgr.api.schedule;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SystemScheduleService {
    @Scheduled(fixedRate = 3000)
    public void printCurrentTime() {
        System.out.println("[Fixed-rate] Hiện tại là: " + LocalDateTime.now());
    }
}
