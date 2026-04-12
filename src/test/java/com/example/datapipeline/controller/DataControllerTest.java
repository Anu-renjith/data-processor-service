package com.example.datapipeline.controller;

import static org.junit.jupiter.api.Assertions.*;



import com.example.datapipeline.constants.DataStatus;
import com.example.datapipeline.dto.DataRequestDto;
import com.example.datapipeline.dto.DataResponseDto;
import com.example.datapipeline.security.JwtTokenProvider;
import com.example.datapipeline.service.DataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DataController.class)
class DataControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    DataService dataService;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    // ── POST /submit ──────────────────────────────────────────────
    // "Spring validates request using @Valid — if fields missing
    //  returns 400. Once validation passes returns 202 Accepted"

    @Test
    @WithMockUser
    void givenValidRequest_whenSubmit_thenReturn202Queued() throws Exception {
        // Given
        DataRequestDto dto = new DataRequestDto();
        dto.setName("test");
        dto.setEmail("test@gmail.com");

        doNothing().when(dataService).submit(any());

        // When + Then
        mockMvc.perform(post("/api/data/submit")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))  //objectMapper converts it to JSON string
                .andExpect(status().isAccepted())        // 202
                .andExpect(content().string("Queued"));
    }

/*    @Test
    @WithMockUser
    void givenMissingName_whenSubmit_thenReturn400() throws Exception {
        // Given — name is missing → @Valid fails
        DataRequestDto dto = new DataRequestDto();
        dto.setName("");   // blank → validation fails
        dto.setEmail("test@gmail.com");

        // When + Then
        mockMvc.perform(post("/api/data/submit")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest()); // 400
    }*/

 /*   @Test
    @WithMockUser
    void givenMissingEmail_whenSubmit_thenReturn400() throws Exception {
        // Given — email is missing → @Valid fails
        DataRequestDto dto = new DataRequestDto();
        dto.setName("test");
        dto.setEmail("");  // blank → validation fails

        // When + Then
        mockMvc.perform(post("/api/data/submit")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest()); // 400 ✅
    }*/

    @Test
    @WithMockUser
    void givenWrongContentType_whenSubmit_thenReturn415() throws Exception {
        // Given — wrong content type → GlobalExceptionHandler catches it
        mockMvc.perform(post("/api/data/submit")
                        .with(csrf())
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("some text"))
                .andExpect(status().isUnsupportedMediaType()); // 415
    }

    @Test
    void givenNoAuth_whenSubmit_thenReturn401() throws Exception {
        // Given — no @WithMockUser = unauthenticated
        DataRequestDto dto = new DataRequestDto();
        dto.setName("test");
        dto.setEmail("test@gmail.com");

        // When + Then
        mockMvc.perform(post("/api/data/submit")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized()); // 401
    }


    // "GET /api/data/{id} hits DataServiceImpl.getData()
    //  First call → cache miss → queries MySQL

    @Test
    @WithMockUser
    void givenExistingId_whenGet_thenReturn200() throws Exception {
        // Given
        DataResponseDto response = new DataResponseDto(
                1L, "test", "test@gmail.com", DataStatus.SUCCESS);

        when(dataService.getData(1L)).thenReturn(response);

        // When + Then
        mockMvc.perform(get("/api/data/1")
                        .with(csrf()))
                .andExpect(status().isOk())                              // 200
                .andExpect(jsonPath("$.id").value(1))                    // id
                .andExpect(jsonPath("$.name").value("test"))             // name
                .andExpect(jsonPath("$.email").value("test@gmail.com"))  // email
                .andExpect(jsonPath("$.status").value("SUCCESS"));       // status
    }

    @Test
    void givenNoAuth_whenGet_thenReturn401() throws Exception {
        // Given — no @WithMockUser = unauthenticated
        mockMvc.perform(get("/api/data/1"))
                .andExpect(status().isUnauthorized()); // 401
    }
}