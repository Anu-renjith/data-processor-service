package com.example.datapipeline.service.impl;

import com.example.datapipeline.dto.DataRequestDto;
import com.example.datapipeline.dto.DataResponseDto;
import com.example.datapipeline.entity.DataEntity;
import com.example.datapipeline.exception.ResourceNotFoundException;
import com.example.datapipeline.repository.DataRepository;
import com.example.datapipeline.service.DataService;
import com.example.datapipeline.service.QueueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
@Slf4j
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
        log.info("Saved entity id={}, pushing to queue", entity.getId());

        queueService.push(entity);
    }

    @Override
    @Cacheable(value = "dataCache", key = "#id")
    public DataResponseDto getData(Long id) {
        log.debug("Cache miss — fetching id={} from DB", id);

        DataEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DataRecord", "id", id));

        return new DataResponseDto(entity.getId(),entity.getName(), entity.getEmail(), entity.getStatus());
    }

    @Override
    public List<DataEntity> getPending() {
        return repository.findByStatus("PENDING");
    }

    @Override
    @CacheEvict(value = "dataCache", key = "#entity.id")
    public void updateStatus(DataEntity entity, String status) {
        entity.setStatus(status);
        repository.save(entity);
    }
}
