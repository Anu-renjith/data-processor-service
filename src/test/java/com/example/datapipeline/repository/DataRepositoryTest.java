package com.example.datapipeline.repository;

import com.example.datapipeline.constants.DataStatus;
import com.example.datapipeline.entity.DataEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
class DataRepositoryTest {
    @Autowired
    DataRepository dataRepository;
    @Test
    public void findByStatus_returnPendingRecords(){
        DataEntity dataEntity=new DataEntity();
        dataEntity.setName("test");
        dataEntity.setEmail("test@gmail.com");
        dataEntity.setStatus(DataStatus.PENDING);
        dataRepository.saveAndFlush(dataEntity);  //save + immediately write to DB
        List<DataEntity> result=dataRepository.findByStatus(DataStatus.PENDING);
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getStatus()).isEqualTo(DataStatus.PENDING);
    }

    @Test
    void findByStatus_returnsSuccessRecords() {
        List<DataEntity> result = dataRepository.findByStatus(DataStatus.SUCCESS);
        assertThat(result).isEmpty();
    }

}