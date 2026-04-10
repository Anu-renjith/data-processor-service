package com.example.datapipeline.scheduler;

import com.example.datapipeline.entity.DataEntity;
import com.example.datapipeline.service.DataService;
import com.example.datapipeline.service.QueueService;
import com.example.datapipeline.service.impl.AsyncProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataScheduler {

    private final DataService dataService;
    private final QueueService queueService;
    private final AsyncProcessingService asyncProcessingService;

    @Scheduled(fixedDelay = 15000)
    public void processQueue() {
        log.info("[scheduler] Triggered");

        // Step 1 — check in-memory queue first
        DataEntity entity = queueService.poll();

        // Step 2 — fallback to DB if queue is empty
        if (entity == null) {
            log.info("[scheduler] Queue empty, checking DB...");
            List<DataEntity> list = dataService.getPending();

            if (list.isEmpty()) {
                log.info("[scheduler] Nothing to process");
                return;
            }

            entity = list.get(0);
            log.info("[scheduler] Picked id={} from DB", entity.getId());
        }

        // Step 3 — hand off immediately, scheduler thread is FREE after this line
        asyncProcessingService.processAsync(entity);
    }
}