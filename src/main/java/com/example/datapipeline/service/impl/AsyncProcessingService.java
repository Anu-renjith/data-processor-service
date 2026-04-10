package com.example.datapipeline.service.impl;

import com.example.datapipeline.entity.DataEntity;
import com.example.datapipeline.service.DataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncProcessingService {

    private final DataService dataService;

    @Value("${app.external.url:https://jsonplaceholder.typicode.com/posts}")
    private String externalUrl;

    @Async  // runs on async thread pool — NOT the scheduler thread
    public void processAsync(DataEntity entity) {
        log.info("async Processing id={} on thread={}",
                entity.getId(), Thread.currentThread().getName());
        try {
            dataService.updateStatus(entity, "PROCESSING");
            callExternalApi(entity);
            dataService.updateStatus(entity, "SUCCESS");
            log.info("async id={} → SUCCESS", entity.getId());
        } catch (Exception e) {
            log.error("async id={} → FAILED: {}", entity.getId(), e.getMessage());
            dataService.updateStatus(entity, "FAILED");
        }
    }

    private void callExternalApi(DataEntity entity) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> payload = new HashMap<>();
        payload.put("name", entity.getName());
        payload.put("email", entity.getEmail());

        log.info("async POSTing id={} to {}", entity.getId(), externalUrl);
        String response = restTemplate.postForObject(externalUrl, payload, String.class);
        log.info("async Response for id={}: {}", entity.getId(), response);
    }
}
