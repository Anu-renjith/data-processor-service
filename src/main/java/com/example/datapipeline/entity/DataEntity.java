package com.example.datapipeline.entity;

import com.example.datapipeline.constants.DataStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "data")
public class DataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DataStatus status; // PENDING, SUCCESS, FAILED

    private LocalDateTime createdAt = LocalDateTime.now();


}
