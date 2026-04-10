package com.example.datapipeline.repository;

import com.example.datapipeline.entity.DataEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DataRepository extends JpaRepository<DataEntity, Long> {
    List<DataEntity> findByStatus(String status);
}