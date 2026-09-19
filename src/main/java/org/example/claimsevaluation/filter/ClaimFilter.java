package org.example.claimsevaluation.filter;

import org.example.claimsevaluation.domain.Claim;
import org.example.claimsevaluation.domain.ClaimDecision;
import org.example.claimsevaluation.domain.Policy;

public interface ClaimFilter {
    /**
     * Validiert einen Anspruch gegen eine Police basierend auf spezifischen Geschäftsregeln.
     *
     * @param claim  Der zu validierende Versicherungsanspruch.
     * @param policy Die Versicherungspolice gegen die validiert wird.
     * @return {@link FilterResult#valid()}, wenn die Validierung erfolgreich war und die Kette fortsetzt
     * oder {@link FilterResult#invalid(ClaimDecision)}, wenn die Validierung fehlgeschlagen ist
     * und die Kette stoppt.
     */
    FilterResult validate(Claim claim, Policy policy);
}
