package com.example.datapipeline.service.impl;

import com.example.datapipeline.entity.DataEntity;
import com.example.datapipeline.service.QueueService;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class QueueServiceImpl implements QueueService {

    private static final BlockingQueue<DataEntity> queue = new LinkedBlockingQueue<>();

    public void push(DataEntity entity) {
        System.out.println("PUSH instance: " + this);

        queue.offer(entity);
    }

    public DataEntity poll() {
        System.out.println("POLL instance: " + this);
        return queue.poll();
    }
}
