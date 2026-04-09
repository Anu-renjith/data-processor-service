package com.example.datapipeline.scheduler;

import com.example.datapipeline.entity.DataEntity;
import com.example.datapipeline.service.DataService;
import com.example.datapipeline.service.QueueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DataScheduler {

    private final QueueService queueService;
    private final DataService dataService;

    public DataScheduler(QueueService queueService, DataService dataService) {
        this.queueService = queueService;
        this.dataService = dataService;
    }

    @Scheduled(fixedDelay = 15000)
    public void processQueue() {

        log.info("Scheduler triggered...");

        // Step 1: Try from queue
        DataEntity entity = queueService.poll();

        // Step 2: Fallback to DB if queue empty
        if (entity == null) {
            log.info("Queue empty, checking DB...");

            List<DataEntity> list = dataService.getPending();

            if (!list.isEmpty()) {
                entity = list.get(0);
                log.info("Fetched from DB: {}", entity.getId());
            } else {
                log.info("No data to process");
                return;
            }
        }


        try {
            log.info("Processing entity id: {}", entity.getId());

            // Step 1: mark as PROCESSING
            dataService.updateStatus(entity, "PROCESSING");

            // Step 2: simulate external API call
            callExternalApi(entity);

            // Step 3: mark success
            dataService.updateStatus(entity, "SUCCESS");

        } catch (Exception e) {
            log.error("Error processing id: {}", entity.getId(), e);
            dataService.updateStatus(entity, "FAILED");
        }
    }

 /*   private void callExternalApi(DataEntity entity) {
        try {
            Thread.sleep(5000); // simulate delay
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }*/

    private void callExternalApi(DataEntity entity) {

        RestTemplate restTemplate = new RestTemplate();

        String url = "https://jsonplaceholder.typicode.com/posts";

        Map<String, Object> request = new HashMap<>();
        request.put("name", entity.getName());
        request.put("email", entity.getEmail());

        String response = restTemplate.postForObject(url, request, String.class);

        System.out.println("Response: " + response);
    }
}