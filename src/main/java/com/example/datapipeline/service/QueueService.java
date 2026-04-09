package com.example.datapipeline.service;

import com.example.datapipeline.entity.DataEntity;

public interface QueueService {
    public void push(DataEntity entity);
    public DataEntity poll();

}
