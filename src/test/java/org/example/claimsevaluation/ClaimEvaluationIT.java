package org.example.claimsevaluation;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.example.claimsevaluation.dto.ClaimRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class ClaimEvaluationIT {

    private static final String ENDPOINT = "/api/claims/evaluate";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String toJson(ClaimRequestDto request) {
        return objectMapper.writeValueAsString(request);
    }

    @Test
    void evaluate_shouldReturnApprovedWithPayout_whenClaimValidAndCovered() throws Exception {
        ClaimRequestDto request = new ClaimRequestDto(
                LocalDate.of(2025, 9, 15),
                "fire",
                BigDecimal.valueOf(500.00),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2026, 12, 31),
                Arrays.asList("fire", "theft"),
                BigDecimal.valueOf(100.00),
                BigDecimal.valueOf(1000.00));

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approved").value(true))
                .andExpect(jsonPath("$.payout").value(400.0))
                .andExpect(jsonPath("$.reason", containsString("Payout")));
    }

    @Test
    void evaluate_shouldCapPayoutAtCoverageLimit_whenPayoutExceedsLimit() throws Exception {
        ClaimRequestDto request = new ClaimRequestDto(
                LocalDate.of(2025, 9, 15),
                "fire",
                BigDecimal.valueOf(500.00),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2026, 12, 31),
                List.of("fire"),
                BigDecimal.valueOf(100.00),
                BigDecimal.valueOf(50.00));

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approved").value(true))
                .andExpect(jsonPath("$.payout").value(50.0))
                .andExpect(jsonPath("$.reason", containsString("capped")));
    }

    @Test
    void evaluate_shouldReturnDenied_whenIncidentTypeNotCovered() throws Exception {
        ClaimRequestDto request = new ClaimRequestDto(
                LocalDate.of(2025, 9, 15),
                "flood",
                BigDecimal.valueOf(500.00),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2026, 12, 31),
                Arrays.asList("fire", "theft"),
                BigDecimal.valueOf(100.00),
                BigDecimal.valueOf(1000.00));

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approved").value(false))
                .andExpect(jsonPath("$.payout").doesNotExist())
                .andExpect(jsonPath("$.reason", containsString("not covered")));
    }

    @Test
    void evaluate_shouldReturnDenied_whenIncidentOutsidePolicyPeriod() throws Exception {
        ClaimRequestDto request = new ClaimRequestDto(
                LocalDate.of(2023, 1, 1),
                "fire",
                BigDecimal.valueOf(500.00),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2026, 12, 31),
                Arrays.asList("fire", "theft"),
                BigDecimal.valueOf(100.00),
                BigDecimal.valueOf(1000.00));

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approved").value(false))
                .andExpect(jsonPath("$.reason", containsString("outside the policy period")));
    }

    @Test
    void evaluate_shouldReturnDenied_whenAmountBelowDeductible() throws Exception {
        ClaimRequestDto request = new ClaimRequestDto(
                LocalDate.of(2025, 9, 15),
                "fire",
                BigDecimal.valueOf(50.00),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2026, 12, 31),
                Arrays.asList("fire", "theft"),
                BigDecimal.valueOf(100.00),
                BigDecimal.valueOf(1000.00));

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approved").value(false))
                .andExpect(jsonPath("$.reason", containsString("does not exceed deductible")));
    }

    @Test
    void evaluate_shouldReturn400_whenRequestInvalid() throws Exception {
        ClaimRequestDto request = new ClaimRequestDto(
                null,
                "fire",
                BigDecimal.valueOf(500.00),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2026, 12, 31),
                Arrays.asList("fire", "theft"),
                BigDecimal.valueOf(100.00),
                BigDecimal.valueOf(1000.00));

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("incidentDate")));
    }

    @Test
    void evaluate_shouldReturn400_whenMultipleFieldsInvalid() throws Exception {
        ClaimRequestDto request = new ClaimRequestDto(
                null,
                null,
                null,
                null,
                null,
                Collections.emptyList(),
                null,
                null);

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }
}
