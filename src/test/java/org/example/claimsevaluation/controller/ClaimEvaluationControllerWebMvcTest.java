package org.example.claimsevaluation.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.example.claimsevaluation.domain.ClaimDecision;
import org.example.claimsevaluation.dto.ClaimRequestDto;
import org.example.claimsevaluation.dto.ClaimResponseDto;
import org.example.claimsevaluation.mapper.ClaimMapper;
import org.example.claimsevaluation.service.ClaimEvaluationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(ClaimEvaluationController.class)
class ClaimEvaluationControllerWebMvcTest {

    private static final String ENDPOINT = "/api/claims/evaluate";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClaimEvaluationService evaluationService;

    @MockitoBean
    private ClaimMapper claimMapper;

    private ClaimRequestDto validRequest() {
        return new ClaimRequestDto(
                LocalDate.of(2025, 9, 15),
                "fire",
                BigDecimal.valueOf(500.00),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2026, 12, 31),
                List.of("fire", "theft"),
                BigDecimal.valueOf(100.00),
                BigDecimal.valueOf(1000.00));
    }

    private ClaimRequestDto invalidRequest() {
        ClaimRequestDto request = validRequest();
        request.setIncidentDate(null);
        return request;
    }

    @Test
    void evaluate_shouldReturn200_whenServiceReturnsDecision() throws Exception {
        when(evaluationService.evaluate(any(), any()))
                .thenReturn(ClaimDecision.approved(BigDecimal.valueOf(400.00), "Payout of 400.00"));
        when(claimMapper.claimDecisionToResponse(any()))
                .thenReturn(new ClaimResponseDto(true, BigDecimal.valueOf(400.00), "Payout of 400.00"));

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approved").value(true))
                .andExpect(jsonPath("$.payout").value(400.0));
    }

    @Test
    void evaluate_shouldReturn400_whenRequestInvalid() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void evaluate_shouldReturn500_whenServiceThrows() throws Exception {
        when(evaluationService.evaluate(any(), any()))
                .thenThrow(new RuntimeException(""));

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message", containsString("Serverfehler")));
    }
}
