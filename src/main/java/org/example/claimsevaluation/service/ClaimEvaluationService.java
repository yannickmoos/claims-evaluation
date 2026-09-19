package org.example.claimsevaluation.service;
import java.util.List;
import java.util.function.Predicate;
import org.example.claimsevaluation.domain.Claim;
import org.example.claimsevaluation.domain.ClaimDecision;
import org.example.claimsevaluation.domain.Policy;
import org.example.claimsevaluation.filter.ClaimFilter;
import org.example.claimsevaluation.filter.FilterResult;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClaimEvaluationService {

    private final List<ClaimFilter> filters;

    /**
     * Bewertet Ansprüche über eine geordnete Kette von {@link ClaimFilter} mit
     * Short-Circuit. Der erste Filter, der {@code invalid} liefert bestimmt die
     * Ablehnung. Sind alle Filter gültig, folgt die finale Genehmigung.
     * <p>
     * Die Filter werden per Dependency Injection in ihrer {@code @Order}-Reihenfolge
     * bereitgestellt.
     * </p>
     */
    public ClaimDecision evaluate(Claim claim, Policy policy) {
        return filters.stream()
                .map(filter -> filter.validate(claim, policy))
                .filter(Predicate.not(FilterResult::isValid))
                .findFirst()
                .map(FilterResult::getRejection)
                .orElseGet(() -> createFinalApproval(claim, policy));
    }

    private ClaimDecision createFinalApproval(Claim claim, Policy policy) {
        var payable = claim.amount().subtract(policy.deductible());
        // Übersteigt der zahlbare Betrag das Deckungslimit, wird auf das Limit gedeckelt
        var payout = payable.min(policy.coverageLimit());

        boolean capped = payout.compareTo(payable) < 0;
        String reason = capped
                ? "Payout capped at coverage limit of " + payout
                : "Payout of " + payout;
        return ClaimDecision.approved(payout, reason);
    }
}
