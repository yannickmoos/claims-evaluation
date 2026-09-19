package org.example.claimsevaluation.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Repräsentiert einen Versicherungsanspruch.
 *
 * @param incidentDate Das Datum des Schadenseintritts.
 * @param incidentType Der Typ des Schadenseintritts (z.B. "fire").
 * @param amount       Der geltend gemachte Schadenbetrag.
 */
public record Claim(LocalDate incidentDate, String incidentType, BigDecimal amount) {
}
