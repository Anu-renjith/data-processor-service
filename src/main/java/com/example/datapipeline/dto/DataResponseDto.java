package com.example.datapipeline.dto;

import com.example.datapipeline.constants.DataStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
public class DataResponseDto {
    private Long id;
    private String name;
    private String email;
    private DataStatus status;

    public DataResponseDto(Long id,String name, String email, DataStatus status) {
        this.id=id;
        this.name = name;
        this.email = email;
        this.status = status;
    }
}
