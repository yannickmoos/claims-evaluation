package org.example.claimsevaluation.domain;

import java.math.BigDecimal;

/**
 * Repräsentiert die finale Entscheidung über einen Versicherungsanspruch.
 *
 * @param approved true, wenn der Anspruch genehmigt wird, false, wenn abgelehnt.
 * @param payout   Der tatsächlich auszuzahlende Betrag bei Genehmigung
 *                 (ggf. auf das Deckungslimit begrenzt) oder null bei Ablehnung.
 * @param reason   Die Begründung der Entscheidung (z.B. "Payout of 400.00"
 *                 oder "Incident date is outside the policy period").
 */
public record ClaimDecision(boolean approved, BigDecimal payout, String reason) {

    /**
     * Erzeugt eine Genehmigung mit auszuzahlendem Betrag.
     *
     * @param payout Der auszuzahlende Betrag
     * @param reason Die Begründung der Genehmigung
     * @return eine genehmigte {@link ClaimDecision}
     */
    public static ClaimDecision approved(BigDecimal payout, String reason) {
        return new ClaimDecision(true, payout, reason);
    }

    /**
     * Erzeugt eine Ablehnung (ohne Auszahlung).
     *
     * @param reason Der Ablehnungsgrund
     * @return eine abgelehnte {@link ClaimDecision}
     */
    public static ClaimDecision denied(String reason) {
        return new ClaimDecision(false, null, reason);
    }

}
