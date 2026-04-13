package com.example.datapipeline.service.impl;

import com.example.datapipeline.constants.DataStatus;
import com.example.datapipeline.dto.DataRequestDto;
import com.example.datapipeline.dto.DataResponseDto;
import com.example.datapipeline.entity.DataEntity;
import com.example.datapipeline.exception.ResourceNotFoundException;
import com.example.datapipeline.repository.DataRepository;
import com.example.datapipeline.service.QueueService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataServiceImplTest {

    @Mock
    DataRepository repository;
    @Mock
    QueueService queueService;
    @InjectMocks
    DataServiceImpl dataService;

    // ── SUBMIT FLOW ─
    // "saves entity to MySQL with status PENDING,
    //  then immediately pushes it onto LinkedBlockingQueue"


    @Test
    void givenValidRequest_whenSubmit_thenPushesToQueueImmediately() {
        //given
        DataRequestDto dto = new DataRequestDto();
        dto.setName("test");
        dto.setEmail("test@gmail.com");

        DataEntity saved = new DataEntity();
        saved.setId(1L);
        saved.setName("test");
        saved.setEmail("test@gmail.com");
        saved.setStatus(DataStatus.PENDING);

        when(repository.save(any())).thenReturn(saved);

        // When
        dataService.submit(dto);

        // Then
        verify(repository, times(1)).save(argThat(entity ->
                entity.getStatus() == DataStatus.PENDING &&
                        entity.getName().equals("test") &&
                        entity.getEmail().equals("test@gmail.com")
        ));                                          // saved with correct data
        verify(queueService, times(1)).push(any(DataEntity.class));  // pushed saved entity with ID
    }

/*    @Test
    void givenValidRequest_whenSubmit_thenSaveHappensBeforeQueuePush() {
        // Given
        DataRequestDto dto = new DataRequestDto();
        dto.setName("test");
        dto.setEmail("test@gmail.com");

        DataEntity saved = new DataEntity();
        saved.setId(1L);
        when(repository.save(any())).thenReturn(saved);

        // When
        dataService.submit(dto);

        // Then — save happens BEFORE queue push (order matters!)
        InOrder inOrder = inOrder(repository, queueService);
        inOrder.verify(repository).save(any());
        inOrder.verify(queueService).push(any());
    }*/

    // SCHEDULER FALLBACK FLOW
    // "If queue is empty after restart — falls back to
    //  querying DB for PENDING records"

    @Test
    void givenPendingRecordsInDb_whenGetPending_thenReturnPendingList() {
        // Given — simulates scheduler DB fallback
        DataEntity entity = new DataEntity();
        entity.setId(1L);
        entity.setStatus(DataStatus.PENDING);

        when(repository.findByStatus(DataStatus.PENDING))
                .thenReturn(List.of(entity));

        // When
        List<DataEntity> result = dataService.getPending();

        // Then — returns PENDING records for scheduler to pick up
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(DataStatus.PENDING);
    }

 /*   @Test
    void givenNoRecordsInDb_whenGetPending_thenReturnEmptyList() {
        // Given — nothing in DB
        when(repository.findByStatus(DataStatus.PENDING))
                .thenReturn(List.of());

        // When
        List<DataEntity> result = dataService.getPending();

        // Then — scheduler sees empty list, does nothing
        assertThat(result).isEmpty();
    }*/

    // ASYNC PROCESSING FLOW
    // "async method marks PROCESSING, calls external API,
    //  then marks SUCCESS or FAILED"

    @Test
    void givenEntity_whenUpdateStatusProcessing_thenMarkedProcessing() {
        // Given
        DataEntity entity = new DataEntity();
        entity.setId(1L);
        entity.setStatus(DataStatus.PENDING);

        // When — async thread marks PROCESSING
        dataService.updateStatus(entity, DataStatus.PROCESSING);

        // Then
        assertThat(entity.getStatus()).isEqualTo(DataStatus.PROCESSING);
        verify(repository, times(1)).save(entity);
    }

    @Test
    void givenEntity_whenUpdateStatusSuccess_thenMarkedSuccess() {
        // Given
        DataEntity entity = new DataEntity();
        entity.setId(1L);
        entity.setStatus(DataStatus.PROCESSING);

        // When — async thread marks SUCCESS after external API call
        dataService.updateStatus(entity, DataStatus.SUCCESS);

        // Then
        assertThat(entity.getStatus()).isEqualTo(DataStatus.SUCCESS);
        verify(repository, times(1)).save(entity);
    }

    @Test
    void givenEntity_whenUpdateStatusFailed_thenMarkedFailed() {
        // Given
        DataEntity entity = new DataEntity();
        entity.setId(1L);
        entity.setStatus(DataStatus.PROCESSING);

        // When — external API throws exception → marked FAILED
        dataService.updateStatus(entity, DataStatus.FAILED);

        // Then
        assertThat(entity.getStatus()).isEqualTo(DataStatus.FAILED);
        verify(repository, times(1)).save(entity);
    }

    //   CACHE READ FLOW
    // "First call → cache miss → queries MySQL
    //  Second call within TTL → served from memory"

    @Test
    void givenExistingId_whenGetData_thenReturnCorrectResponse() {
        // Given — first call, cache miss, hits DB
        DataEntity entity = new DataEntity();
        entity.setId(1L);
        entity.setName("test");
        entity.setEmail("test@gmail.com");
        entity.setStatus(DataStatus.SUCCESS);

        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        // When
        DataResponseDto result = dataService.getData(1L);

        // Then — correct data returned
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("test");
        assertThat(result.getEmail()).isEqualTo("test@gmail.com");
        assertThat(result.getStatus()).isEqualTo(DataStatus.SUCCESS);
        verify(repository, times(1)).findById(1L); // DB was hit
    }

    @Test
    void givenNonExistingId_whenGetData_thenThrowResourceNotFoundException() {
        // Given — id not found in DB or cache
        when(repository.findById(1L)).thenReturn(Optional.empty());

        // When + Then — ResourceNotFoundException thrown
        assertThatThrownBy(() -> dataService.getData(1L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repository, times(1)).findById(1L);
    }
}