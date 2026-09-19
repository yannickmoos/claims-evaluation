package org.example.claimsevaluation.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.example.claimsevaluation.domain.Claim;
import org.example.claimsevaluation.domain.Policy;
import org.junit.jupiter.api.Test;

class PolicyPeriodFilterTest {

    private final PolicyPeriodFilter filter = new PolicyPeriodFilter();

    @Test
    void validate_shouldReturnValid_whenDateWithinPeriod() {
        Claim claim = claimOn(LocalDate.of(2025, 6, 1));

        FilterResult result = filter.validate(claim, policy);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    void validate_shouldReturnValid_whenDateOnBoundary() {
        Claim claimOnStart = claimOn(LocalDate.of(2024, 1, 1));
        Claim claimOnEnd = claimOn(LocalDate.of(2026, 12, 31));

        FilterResult startResult = filter.validate(claimOnStart, policy);
        FilterResult endResult = filter.validate(claimOnEnd, policy);

        assertThat(startResult.isValid()).isTrue();
        assertThat(endResult.isValid()).isTrue();
    }

    @Test
    void validate_shouldReturnInvalid_whenDateBeforeStart() {
        Claim claim = claimOn(LocalDate.of(2023, 12, 31));

        FilterResult result = filter.validate(claim, policy);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getRejection().approved()).isFalse();
    }

    @Test
    void validate_shouldReturnInvalid_whenDateAfterEnd() {
        Claim claim = claimOn(LocalDate.of(2027, 1, 1));

        FilterResult result = filter.validate(claim, policy);

        assertThat(result.isValid()).isFalse();
    }

    private final Policy policy = new Policy(
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2026, 12, 31),
            List.of("fire"),
            new BigDecimal("100.00"),
            new BigDecimal("1000.00"));

    private Claim claimOn(LocalDate date) {
        return new Claim(date, "fire", new BigDecimal("500.00"));
    }
}
