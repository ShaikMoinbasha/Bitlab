package com.talentstream.healthcheck;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HealthCheckScheduler {
 
    private final HealthCheckService healthCheckService;
    
    
    private static final Logger LOGGER=LoggerFactory.getLogger(HealthCheckScheduler.class);
 
    public HealthCheckScheduler(HealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }
 
   
    @Scheduled(fixedRate = 10000000) // 10 seconds interval
  

    public void healthCheck() {
       
        LOGGER.debug("Performing backend health check...");
        if (healthCheckService.isBackendHealthy()) {
            System.out.println("Backend is healthy");
        } else {
            System.out.println("Backend is not healthy");
        }
    }
}