package com.example.datapipeline.dto;

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
    private String status;

    public DataResponseDto(Long id,String name, String email, String status) {
        this.id=id;
        this.name = name;
        this.email = email;
        this.status = status;
    }
}
