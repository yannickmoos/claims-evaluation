package org.example.claimsevaluation.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.example.claimsevaluation.domain.Claim;
import org.example.claimsevaluation.domain.ClaimDecision;
import org.example.claimsevaluation.domain.Policy;
import org.example.claimsevaluation.filter.DeductibleFilter;
import org.example.claimsevaluation.filter.IncidentTypeFilter;
import org.example.claimsevaluation.filter.PolicyPeriodFilter;
import org.junit.jupiter.api.Test;

class ClaimEvaluationServiceTest {

    private final ClaimEvaluationService service = new ClaimEvaluationService(List.of(
            new PolicyPeriodFilter(),
            new IncidentTypeFilter(),
            new DeductibleFilter()));

    private Policy policy(BigDecimal deductible, BigDecimal coverageLimit) {
        return new Policy(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2026, 12, 31),
                List.of("fire", "theft"),
                deductible,
                coverageLimit);
    }

    private Claim claim(LocalDate date, String type, BigDecimal amount) {
        return new Claim(date, type, amount);
    }

    @Test
    void evaluate_shouldApproveWithPayoutMinusDeductible_whenAllFiltersPass() {
        var decision = service.evaluate(
                claim(LocalDate.of(2025, 6, 1), "fire", new BigDecimal("500.00")),
                policy(new BigDecimal("100.00"), new BigDecimal("1000.00")));

        assertThat(decision.approved()).isTrue();
        assertThat(decision.payout()).isEqualByComparingTo("400.00");
        assertThat(decision.reason()).isEqualTo("Payout of 400.00");
    }

    @Test
    void evaluate_shouldCapPayoutAtCoverageLimit_whenPayoutExceedsLimit() {
        var decision = service.evaluate(
                claim(LocalDate.of(2025, 6, 1), "fire", new BigDecimal("2000.00")),
                policy(new BigDecimal("100.00"), new BigDecimal("1000.00")));

        assertThat(decision.approved()).isTrue();
        assertThat(decision.payout()).isEqualByComparingTo("1000.00");
        assertThat(decision.reason()).isEqualTo("Payout capped at coverage limit of 1000.00");
    }

    @Test
    void evaluate_shouldDeny_whenAmountDoesNotExceedDeductible() {
        var decision = service.evaluate(
                claim(LocalDate.of(2025, 6, 1), "fire", new BigDecimal("100.00")),
                policy(new BigDecimal("100.00"), new BigDecimal("1000.00")));

        assertThat(decision.approved()).isFalse();
        assertThat(decision.payout()).isNull();
        assertThat(decision.reason()).contains("does not exceed deductible");
    }

    @Test
    void evaluate_shouldDeny_whenIncidentTypeNotCovered() {
        var decision = service.evaluate(
                claim(LocalDate.of(2025, 6, 1), "flood", new BigDecimal("500.00")),
                policy(new BigDecimal("100.00"), new BigDecimal("1000.00")));

        assertThat(decision.approved()).isFalse();
        assertThat(decision.reason()).contains("not covered");
    }

    @Test
    void evaluate_shouldDeny_whenIncidentOutsidePolicyPeriod() {
        var decision = service.evaluate(
                claim(LocalDate.of(2030, 1, 1), "fire", new BigDecimal("500.00")),
                policy(new BigDecimal("100.00"), new BigDecimal("1000.00")));

        assertThat(decision.approved()).isFalse();
        assertThat(decision.reason()).contains("outside the policy period");
    }

    @Test
    void evaluate_shouldDenyWithFirstFailingFilter_whenMultipleFiltersFail() {
        ClaimDecision decision = service.evaluate(
                claim(LocalDate.of(2030, 1, 1), "flood", new BigDecimal("500.00")),
                policy(new BigDecimal("100.00"), new BigDecimal("1000.00")));

        assertThat(decision.approved()).isFalse();
        assertThat(decision.reason()).contains("outside the policy period");
    }
}
