package com.example.datapipeline.service.impl;

import com.example.datapipeline.entity.DataEntity;
import com.example.datapipeline.service.QueueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Service
public class QueueServiceImpl implements QueueService {

    private static final BlockingQueue<DataEntity> queue = new LinkedBlockingQueue<>();

    public void push(DataEntity entity) {
        log.debug("Queue PUSH id={}, instance={}", entity.getId(), this);

        queue.offer(entity);
    }

    public DataEntity poll() {
        DataEntity entity = queue.poll();
        log.debug("Queue POLL id={}, instance={}", entity != null ? entity.getId() : "empty", this);
        return entity;
    }
}
