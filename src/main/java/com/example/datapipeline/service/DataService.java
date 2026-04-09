package com.example.datapipeline.service;

import com.example.datapipeline.dto.DataRequestDto;
import com.example.datapipeline.dto.DataResponseDto;
import com.example.datapipeline.entity.DataEntity;

import java.util.List;

public interface DataService {
    public void submit(DataRequestDto dto);
    public DataResponseDto getData(Long id);
    public List<DataEntity> getPending();
    public void updateStatus(DataEntity entity, String status);

}
