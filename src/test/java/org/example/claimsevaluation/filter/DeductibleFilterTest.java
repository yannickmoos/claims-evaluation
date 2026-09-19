package org.example.claimsevaluation.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.example.claimsevaluation.domain.Claim;
import org.example.claimsevaluation.domain.Policy;
import org.junit.jupiter.api.Test;

class DeductibleFilterTest {

    private final DeductibleFilter filter = new DeductibleFilter();

    @Test
    void validate_shouldReturnValid_whenAmountExceedsDeductible() {
        Claim claim = claimOf(new BigDecimal("150.00"));
        Policy policy = policyWithDeductible(new BigDecimal("100.00"));

        FilterResult result = filter.validate(claim, policy);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    void validate_shouldReturnInvalid_whenAmountEqualsDeductible() {
        Claim claim = claimOf(new BigDecimal("100.00"));
        Policy policy = policyWithDeductible(new BigDecimal("100.00"));

        FilterResult result = filter.validate(claim, policy);

        assertThat(result.isValid()).isFalse();
    }

    @Test
    void validate_shouldReturnInvalid_whenAmountBelowDeductible() {
        Claim claim = claimOf(new BigDecimal("50.00"));
        Policy policy = policyWithDeductible(new BigDecimal("100.00"));

        FilterResult result = filter.validate(claim, policy);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getRejection().reason()).contains("does not exceed deductible");
    }

    private Policy policyWithDeductible(BigDecimal deductible) {
        return new Policy(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2026, 12, 31),
                List.of("fire"),
                deductible,
                new BigDecimal("1000.00"));
    }

    private Claim claimOf(BigDecimal amount) {
        return new Claim(LocalDate.of(2025, 6, 1), "fire", amount);
    }
}
