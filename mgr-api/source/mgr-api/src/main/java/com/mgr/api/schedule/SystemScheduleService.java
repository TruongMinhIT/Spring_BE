package com.mgr.api.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class SystemScheduleService {
//    @Scheduled(fixedRate = 7000)
    public void printCurrentTime() {
        System.out.println("[Fixed-rate] Hiện tại là: " + LocalDateTime.now());
    }

//    @Scheduled(fixedDelay = 2000)
    public void printDemofixedDelay() throws InterruptedException {
        System.out.println("[Fixed-rate] Hiện tại là: " + LocalDateTime.now());
        Thread.sleep(3000);
    }

    @Scheduled(cron = "0 0 12 * * *")
    public void demoCron() {
        System.out.println("[Fixed-rate] Hiện tại là: " + LocalDateTime.now());
    }

//    @Scheduled(fixedDelay = 6000)
    public void printDemofixedDelay2() throws InterruptedException {
        System.out.println("[Fixed-rate] Hiện tại là: " + LocalDateTime.now());
    }

    @Scheduled(cron = "0 0 13 * * *")
    public void demoCron2() {
        System.out.println("[Fixed-rate] Hiện tại là: " + LocalDateTime.now());
    }
}
