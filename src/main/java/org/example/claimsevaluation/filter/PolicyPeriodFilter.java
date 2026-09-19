package org.example.claimsevaluation.filter;

import org.example.claimsevaluation.domain.Claim;
import org.example.claimsevaluation.domain.ClaimDecision;
import org.example.claimsevaluation.domain.Policy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Prüft, ob das Schadendatum innerhalb des Versicherungszeitraums liegt.
 */
@Component
@Order(1)
public class PolicyPeriodFilter implements ClaimFilter {
    private static final String MESSAGE_OUTSIDE_PERIOD =
            "Incident date %s is outside the policy period";

    @Override
    public FilterResult validate(Claim claim, Policy policy) {
        if (claim.incidentDate().isBefore(policy.startDate())
                || claim.incidentDate().isAfter(policy.endDate())) {
            return FilterResult.invalid(ClaimDecision.denied(
                    String.format(MESSAGE_OUTSIDE_PERIOD, claim.incidentDate())));
        }
        return FilterResult.valid();
    }
}
