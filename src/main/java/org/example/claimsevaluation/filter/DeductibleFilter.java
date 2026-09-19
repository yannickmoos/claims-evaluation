package org.example.claimsevaluation.filter;

import org.example.claimsevaluation.domain.Claim;
import org.example.claimsevaluation.domain.ClaimDecision;
import org.example.claimsevaluation.domain.Policy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Prüft, ob der Anspruchsbetrag die Selbstbeteiligung übersteigt.
 */
@Component
@Order(3)
public class DeductibleFilter implements ClaimFilter {
    private static final String MESSAGE_BELOW_DEDUCTIBLE =
            "Claim amount of %s does not exceed deductible of %s";

    @Override
    public FilterResult validate(Claim claim, Policy policy) {
        if (claim.amount().compareTo(policy.deductible()) <= 0) {
            return FilterResult.invalid(ClaimDecision.denied(
                    String.format(MESSAGE_BELOW_DEDUCTIBLE, claim.amount(), policy.deductible())));
        }
        return FilterResult.valid();
    }
}
