package org.example.claimsevaluation.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repräsentiert eine Versicherungspolice.
 *
 * @param startDate            Der Start-Tag der Versicherungsdeckung.
 * @param endDate              Der End-Tag der Versicherungsdeckung.
 * @param coveredIncidentTypes Liste der abgedeckten Schadenstypen (z.B. "fire").
 * @param deductible           Der Betrag, den der Versicherte selbst tragen muss.
 * @param coverageLimit        Der maximale Deckungsbetrag für einen Schadensfall.
 */
public record Policy(LocalDate startDate, LocalDate endDate, List<String> coveredIncidentTypes, BigDecimal deductible,
                     BigDecimal coverageLimit) {
}
