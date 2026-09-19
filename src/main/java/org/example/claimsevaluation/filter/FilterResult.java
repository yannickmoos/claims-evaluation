package org.example.claimsevaluation.filter;

import org.example.claimsevaluation.domain.ClaimDecision;

public class FilterResult {
    private final boolean valid;
    private final ClaimDecision rejection;

    private FilterResult(boolean valid, ClaimDecision rejection) {
        this.valid = valid;
        this.rejection = rejection;
    }

    /**
     * Erzeugt ein erfolgreiches Filter-Ergebnis.
     * Die Filter-Kette wird fortgesetzt.
     *
     * @return Ein {@link FilterResult} das Erfolg signalisiert (isValid() == true)
     */
    public static FilterResult valid() {
        return new FilterResult(true, null);
    }

    /**
     * Erzeugt ein fehlgeschlagenes Filter-Ergebnis mit Ablehnungs-Informationen.
     * Die Filter-Kette stoppt hier. Nachfolgende Filter werden nicht verarbeitet (Short-Circuit).
     *
     * @param rejection Die {@link ClaimDecision} mit Ablehnungsgrund.
     * @return Ein {@link FilterResult}, dass Fehler signalisiert (isValid() == false)
     */
    public static FilterResult invalid(ClaimDecision rejection) {
        return new FilterResult(false, rejection);
    }

    public boolean isValid() {
        return valid;
    }

    public ClaimDecision getRejection() {
        return rejection;
    }
}
