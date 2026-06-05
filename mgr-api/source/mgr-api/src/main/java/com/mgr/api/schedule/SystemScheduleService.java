package com.mgr.api.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class SystemScheduleService {
    @Scheduled(fixedRate = 7000)
    public void printCurrentTime() {
        System.out.println("[Fixed-rate] Hiện tại là: " + LocalDateTime.now());
    }
}
