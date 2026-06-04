package com.mgr.api.config;

import feign.Retryer;
import org.springframework.context.annotation.Bean;

public class RetrytFeignConfig {
    @Bean
    public Retryer feignRetryer() {
        // period: Waiting time before the FIRST attempt
        // maxPeriod: MAXIMUM waiting time between attemps
        // maxAttempts: MAXIMUM total number of attemps
        return new Retryer.Default(1000, 3000, 3);
    }
}
