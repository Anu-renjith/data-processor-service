package com.example.datapipeline.controller;
//Submit request → queue → DB

import com.example.datapipeline.dto.DataRequestDto;
import com.example.datapipeline.dto.DataResponseDto;
import com.example.datapipeline.service.DataService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/data")
public class DataController {

    private final DataService service;

    public DataController(DataService service) {
        this.service = service;
    }
    /**
     * Submit data to be queued and persisted.
     * Accepts JSON only. Validates request fields.
     * Returns 202 Accepted immediately — processing happens asynchronously.
     */
    @PostMapping(value = "/submit",
            consumes = "application/json",
            produces = "application/json")  //ensure json only allowed
    public ResponseEntity<String> submit(@Valid @RequestBody DataRequestDto dto) {
        service.submit(dto);
        return ResponseEntity.accepted().body("Queued");
    }

    /**
     * Retrieve data by ID.
     * Served from Caffeine cache if available (TTL 5 min).
     * Falls back to DB on cache miss.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DataResponseDto> get(@PathVariable Long id) {

        return ResponseEntity.ok(service.getData(id));
    }
}
