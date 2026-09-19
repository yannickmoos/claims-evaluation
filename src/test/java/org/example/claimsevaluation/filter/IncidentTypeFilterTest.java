package org.example.claimsevaluation.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.example.claimsevaluation.domain.Claim;
import org.example.claimsevaluation.domain.Policy;
import org.junit.jupiter.api.Test;

class IncidentTypeFilterTest {

    private final IncidentTypeFilter filter = new IncidentTypeFilter();

    @Test
    void validate_shouldReturnValid_whenTypeCovered() {
        Claim fireClaim = claimOfType("fire");
        Claim theftClaim = claimOfType("theft");

        FilterResult fireResult = filter.validate(fireClaim, policy);
        FilterResult theftResult = filter.validate(theftClaim, policy);

        assertThat(fireResult.isValid()).isTrue();
        assertThat(theftResult.isValid()).isTrue();
    }

    @Test
    void validate_shouldReturnInvalid_whenTypeNotCovered() {
        Claim claim = claimOfType("flood");

        FilterResult result = filter.validate(claim, policy);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getRejection().reason()).contains("not covered");
    }

    private final Policy policy = new Policy(
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2026, 12, 31),
            List.of("fire", "theft"),
            new BigDecimal("100.00"),
            new BigDecimal("1000.00"));

    private Claim claimOfType(String type) {
        return new Claim(LocalDate.of(2025, 6, 1), type, new BigDecimal("500.00"));
    }
}
