package com.example.datapipeline.service.impl;

import com.example.datapipeline.dto.DataRequestDto;
import com.example.datapipeline.dto.DataResponseDto;
import com.example.datapipeline.entity.DataEntity;
import com.example.datapipeline.repository.DataRepository;
import com.example.datapipeline.service.DataService;
import com.example.datapipeline.service.QueueService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataServiceImpl implements DataService {

    private final DataRepository repository;
    private final QueueService queueService;

    public DataServiceImpl(DataRepository repository, QueueService queueService) {
        this.repository = repository;
        this.queueService = queueService;
    }

    @Override
    public void submit(DataRequestDto dto) {
        DataEntity entity = new DataEntity();
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setStatus("PENDING");

        repository.save(entity);

        queueService.push(entity);
    }

    @Override
    @Cacheable(value = "dataCache", key = "#id")
    public DataResponseDto getData(Long id) {
        DataEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));

        return new DataResponseDto(entity.getName(), entity.getEmail(), entity.getStatus());
    }

    @Override
    public List<DataEntity> getPending() {
        return repository.findByStatus("PENDING");
    }

    @Override
    public void updateStatus(DataEntity entity, String status) {
        entity.setStatus(status);
        repository.save(entity);
    }
}
