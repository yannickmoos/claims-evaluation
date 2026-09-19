package org.example.claimsevaluation.controller;

import jakarta.validation.Valid;

import org.example.claimsevaluation.domain.Claim;
import org.example.claimsevaluation.domain.ClaimDecision;
import org.example.claimsevaluation.domain.Policy;
import org.example.claimsevaluation.dto.ClaimRequestDto;
import org.example.claimsevaluation.dto.ClaimResponseDto;
import org.example.claimsevaluation.mapper.ClaimMapper;
import org.example.claimsevaluation.service.ClaimEvaluationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller zur Evaluation von Versicherungsansprüchen.
 */
@Slf4j
@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimEvaluationController {

    private final ClaimEvaluationService evaluationService;
    private final ClaimMapper claimMapper;

    /**
     * Evaluiert einen Versicherungsanspruch gegen eine Police.
     *
     * @param claimRequest Das zu validierende Evaluierungsanfrage DTO (@Valid aktiviert Jakarta Validation)
     * @return ResponseEntity mit dem Evaluierungsergebnis
     */
    @PostMapping("/evaluate")
    public ResponseEntity<ClaimResponseDto> evaluateClaim(@Valid @RequestBody ClaimRequestDto claimRequest) {
        log.info("Evaluating claim for incident type: {}", claimRequest.getIncidentType());

        Claim claim = claimMapper.claimRequestToClaim(claimRequest);
        Policy policy = claimMapper.claimRequestToPolicy(claimRequest);

        ClaimDecision decision = evaluationService.evaluate(claim, policy);

        ClaimResponseDto response = claimMapper.claimDecisionToResponse(decision);

        log.info("Evaluation completed - Approved: {}", response.isApproved());
        return ResponseEntity.ok(response);
    }
}
