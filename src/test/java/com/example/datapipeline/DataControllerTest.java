package com.example.datapipeline;


import com.example.datapipeline.controller.DataController;
import com.example.datapipeline.dto.DataRequestDto;
import com.example.datapipeline.dto.DataResponseDto;
import com.example.datapipeline.security.JwtAuthenticationFilter;
import com.example.datapipeline.security.JwtTokenProvider;
import com.example.datapipeline.service.DataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = DataController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        }
)
class DataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DataService dataService;

    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // ── POST /api/data/submit ─────────────────────────────────────────────────

    @Test
    void submit_validRequest_returns202Queued() throws Exception {
        DataRequestDto dto = new DataRequestDto();
        dto.setName("Alice");
        dto.setEmail("alice@example.com");

        doNothing().when(dataService).submit(any(DataRequestDto.class));

        mockMvc.perform(post("/api/data/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isAccepted())
                .andExpect(content().string("Queued"));

        verify(dataService, times(1)).submit(any(DataRequestDto.class));
    }

    @Test
    void submit_nonJsonContentType_returns415() throws Exception {
        mockMvc.perform(post("/api/data/submit")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("name=Alice&email=alice@example.com"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void submit_malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/api/data/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submit_missingRequiredFields_returns400WithValidationErrors() throws Exception {
        // empty body — validation annotations on DTO should fire
        DataRequestDto dto = new DataRequestDto(); // name/email null

        mockMvc.perform(post("/api/data/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // ── GET /api/data/{id} ────────────────────────────────────────────────────

    @Test
    void get_existingId_returns200WithData() throws Exception {
        DataResponseDto response = new DataResponseDto();
        response.setId(1L);
        response.setName("Alice");
        response.setEmail("alice@example.com");
        response.setStatus("SUCCESS");

        when(dataService.getData(1L)).thenReturn(response);

        mockMvc.perform(get("/api/data/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void get_nonExistentId_returns404() throws Exception {
        when(dataService.getData(99L))
                .thenThrow(new com.example.datapipeline.exception.ResourceNotFoundException("Data", "id", 99L));

        mockMvc.perform(get("/api/data/99"))
                .andExpect(status().isNotFound());
    }
}
