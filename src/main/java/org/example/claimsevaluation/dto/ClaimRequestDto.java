package org.example.claimsevaluation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimRequestDto {
    /**
     * Das Datum des Schadenseintritts (Format: "YYYY-MM-DD").
     */
    @NotNull(message = "incidentDate ist erforderlich")
    private LocalDate incidentDate;

    /**
     * Der Typ des Schadenseintritts (z.B. "fire").
     */
    @NotNull(message = "incidentType ist erforderlich")
    @NotEmpty(message = "incidentType darf nicht leer sein")
    private String incidentType;

    /**
     * Der geltend gemachte Schadenbetrag.
     */
    @NotNull(message = "amount ist erforderlich")
    @DecimalMin(value = "0.01", message = "amount muss > 0 sein")
    private BigDecimal amount;

    /**
     * Der Start-Tag der Versicherungsdeckung (Format: "YYYY-MM-DD").
     */
    @NotNull(message = "policyStartDate ist erforderlich")
    private LocalDate policyStartDate;

    /**
     * Der End-Tag der Versicherungsdeckung (Format: "YYYY-MM-DD").
     */
    @NotNull(message = "policyEndDate ist erforderlich")
    private LocalDate policyEndDate;

    /**
     * Liste der abgedeckten Schadenstypen (z.B. ["fire"]).
     */
    @NotNull(message = "coveredIncidentTypes ist erforderlich")
    @NotEmpty(message = "coveredIncidentTypes darf nicht leer sein")
    private List<String> coveredIncidentTypes;

    /**
     * Der Selbstbeteiligungsbetrag.
     */
    @NotNull(message = "deductible ist erforderlich")
    @DecimalMin(value = "0", message = "deductible darf nicht negativ sein")
    private BigDecimal deductible;

    /**
     * Das maximale Deckungslimit.
     */
    @NotNull(message = "coverageLimit ist erforderlich")
    @DecimalMin(value = "0.01", message = "coverageLimit muss > 0 sein")
    private BigDecimal coverageLimit;
}
