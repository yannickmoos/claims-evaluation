package org.example.claimsevaluation.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimResponseDto {
    /**
     * Die finale Entscheidung: true = genehmigt, false = abgelehnt.
     */
    private boolean approved;

    /**
     * Der auszuzahlende Betrag (ggf. auf das Deckungslimit begrenzt)
     * oder null bei Ablehnung.
     */
    private BigDecimal payout;

    /**
     * Die Begründung der Entscheidung (z.B. "Payout of 400.00" oder
     * "Incident date is outside the policy period").
     */
    private String reason;

}
