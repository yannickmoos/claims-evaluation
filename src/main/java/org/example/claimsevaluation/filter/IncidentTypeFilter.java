package org.example.claimsevaluation.filter;

import org.example.claimsevaluation.domain.Claim;
import org.example.claimsevaluation.domain.ClaimDecision;
import org.example.claimsevaluation.domain.Policy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Prüft, ob der Schadenstyp durch die Police gedeckt ist.
 */
@Component
@Order(2)
public class IncidentTypeFilter implements ClaimFilter {
    private static final String MESSAGE_NOT_COVERED =
            "Incident type '%s' is not covered by this policy";

    @Override
    public FilterResult validate(Claim claim, Policy policy) {
        if (!policy.coveredIncidentTypes().contains(claim.incidentType())) {
            return FilterResult.invalid(ClaimDecision.denied(
                    String.format(MESSAGE_NOT_COVERED, claim.incidentType())));
        }
        return FilterResult.valid();
    }
}
