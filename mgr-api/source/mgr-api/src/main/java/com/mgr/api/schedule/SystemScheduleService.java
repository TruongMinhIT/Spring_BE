package com.mgr.api.schedule;

import com.mgr.api.repository.NewsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class SystemScheduleService {
    @Autowired
    private NewsRepository newsRepository;

    @Scheduled(fixedRate = 7000)
    public void printCurrentTime() {
        log.info("[Fixed-rate] Hiện tại là: {}", LocalDateTime.now());
    }

    @Scheduled(cron = "0 0 12 * * ?")
    public void printTotalNewsEveryNoon() {
        long totalNews = newsRepository.count();
        log.info("[CRON-12g]: Hệ thống đang có {} news", totalNews);
    }
}
