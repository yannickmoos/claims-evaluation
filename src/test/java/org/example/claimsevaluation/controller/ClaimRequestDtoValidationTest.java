package org.example.claimsevaluation.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.example.claimsevaluation.dto.ClaimRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ClaimRequestDtoValidationTest {

    @Autowired
    private Validator validator;

    @Test
    void validate_shouldReturnNoViolations_whenRequestValid() {
        ClaimRequestDto request = new ClaimRequestDto(
                LocalDate.of(2025, 9, 15),
                "fire",
                BigDecimal.valueOf(500.00),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2026, 12, 31),
                Arrays.asList("fire", "theft"),
                BigDecimal.valueOf(100.00),
                BigDecimal.valueOf(1000.00)
        );

        Set<ConstraintViolation<ClaimRequestDto>> violations = validator.validate(request);

        assertTrue(violations.isEmpty(), "Valide Anfrage sollte keine Violations haben");
    }

    @Test
    void validate_shouldReturnViolation_whenIncidentDateNull() {
        ClaimRequestDto request = new ClaimRequestDto(
                null,
                "fire",
                BigDecimal.valueOf(500.00),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2026, 12, 31),
                Arrays.asList("fire", "theft"),
                BigDecimal.valueOf(100.00),
                BigDecimal.valueOf(1000.00)
        );

        Set<ConstraintViolation<ClaimRequestDto>> violations = validator.validate(request);

        assertFalse(violations.isEmpty(), "Sollte @NotNull Verletzung finden");
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("incidentDate")),
                "Sollte incidentDate Feld markieren");
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getMessage().contains("erforderlich")),
                "Sollte 'erforderlich' Nachricht enthalten");
    }

    @Test
    void validate_shouldReturnViolation_whenAmountNull() {
        ClaimRequestDto request = new ClaimRequestDto(
                LocalDate.of(2025, 9, 15),
                "fire",
                null,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2026, 12, 31),
                Arrays.asList("fire", "theft"),
                BigDecimal.valueOf(100.00),
                BigDecimal.valueOf(1000.00)
        );

        Set<ConstraintViolation<ClaimRequestDto>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("amount")));
    }

    @Test
    void validate_shouldReturnViolation_whenCoveredIncidentTypesEmpty() {
        ClaimRequestDto request = new ClaimRequestDto(
                LocalDate.of(2025, 9, 15),
                "fire",
                BigDecimal.valueOf(500.00),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2026, 12, 31),
                Collections.emptyList(),
                BigDecimal.valueOf(100.00),
                BigDecimal.valueOf(1000.00)
        );

        Set<ConstraintViolation<ClaimRequestDto>> violations = validator.validate(request);

        assertFalse(violations.isEmpty(), "Sollte @NotEmpty Verletzung finden");
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("coveredIncidentTypes")),
                "Sollte coveredIncidentTypes Feld markieren");
    }

    @Test
    void validate_shouldReturnViolation_whenAmountNegative() {
        ClaimRequestDto request = new ClaimRequestDto(
                LocalDate.of(2025, 9, 15),
                "fire",
                BigDecimal.valueOf(-500.00),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2026, 12, 31),
                Arrays.asList("fire", "theft"),
                BigDecimal.valueOf(100.00),
                BigDecimal.valueOf(1000.00)
        );

        Set<ConstraintViolation<ClaimRequestDto>> violations = validator.validate(request);

        assertFalse(violations.isEmpty(), "Sollte @DecimalMin Verletzung finden");
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("amount")),
                "Sollte amount Feld markieren");
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getMessage().contains("> 0")),
                "Sollte Minimum-Constraint erwähnen");
    }

    @Test
    void validate_shouldReturnMultipleViolations_whenMultipleFieldsNull() {
        ClaimRequestDto request = new ClaimRequestDto(
                null,
                null,
                null,
                null,
                null,
                Collections.emptyList(),
                null,
                null
        );

        Set<ConstraintViolation<ClaimRequestDto>> violations = validator.validate(request);

        assertFalse(violations.isEmpty(), "Sollte mehrere Verletzungen finden");
        assertTrue(violations.size() >= 5, "Sollte mindestens 5 Violations haben");
    }

    @Test
    void validate_shouldReturnNoDeductibleViolation_whenDeductibleZero() {
        ClaimRequestDto request = new ClaimRequestDto(
                LocalDate.of(2025, 9, 15),
                "fire",
                BigDecimal.valueOf(500.00),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2026, 12, 31),
                Arrays.asList("fire", "theft"),
                BigDecimal.ZERO,
                BigDecimal.valueOf(1000.00)
        );

        Set<ConstraintViolation<ClaimRequestDto>> violations = validator.validate(request);

        assertFalse(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("deductible")));
    }
}
