# ClaimsEvaluation

Ein Spring-Boot-Service zur **automatisierten Bewertung von Versicherungsansprüchen (Claims)**.
Ein eingehender Schadensfall wird gegen die Bedingungen einer Versicherungspolice geprüft
und das Ergebnis (Genehmigung mit Auszahlungsbetrag oder Ablehnung mit Begründung) zurückgegeben.

---

## Fachlicher Hintergrund

In der Schadenbearbeitung einer Versicherung muss jeder gemeldete Anspruch (*Claim*)
gegen die zugehörige Police (*Policy*) geprüft werden, bevor eine Auszahlung erfolgt.
Dieser Service kapselt diese Prüfung als nachvollziehbare, testbare Regelkette.


| Begriff | Bedeutung |
|---------|-----------|
| **Claim** | Ein gemeldeter Schadensfall: Datum, Typ und geltend gemachter Betrag. |
| **Policy** | Die Versicherungspolice: Deckungszeitraum, gedeckte Schadenstypen, Selbstbeteiligung (*deductible*) und Deckungslimit (*coverage limit*). |
| **Deductible** | Selbstbeteiligung – den Anteil, den der Versicherte selbst trägt. |
| **Coverage Limit** | Maximaler Betrag, den die Versicherung pro Schadensfall auszahlt. |
| **Payout** | Der tatsächlich auszuzahlende Betrag. |
| **ClaimDecision** | Das Ergebnis der Bewertung (genehmigt/abgelehnt + Betrag + Begründung). |

---

## Geschäftsregeln

Ein Anspruch wird über eine **geordnete Filterkette** mit **Short-Circuit** geprüft.
Sobald eine Regel verletzt ist, stoppt die Kette und der Anspruch wird **abgelehnt**
(die restlichen Regeln werden nicht mehr geprüft). Bestehen alle Regeln, wird der
Auszahlungsbetrag berechnet.

| # | Regel | Bedingung | Verletzung |
|---|-------|-----------|------------|
| 1 | **Policenzeitraum** | `policyStartDate ≤ incidentDate ≤ policyEndDate` (Grenzen inklusiv) | Ablehnung: *„outside the policy period"* |
| 2 | **Gedeckter Schadenstyp** | `incidentType ∈ coveredIncidentTypes` | Ablehnung: *„not covered by this policy"* |
| 3 | **Selbstbeteiligung** | `amount > deductible` (Gleichheit reicht **nicht**) | Ablehnung: *„does not exceed deductible"* |

**Auszahlungsberechnung** (wenn alle Regeln bestehen):

```
payable = amount - deductible
payout  = min(payable, coverageLimit)   // Deckelung auf das Deckungslimit
```

- Ist `payable ≤ coverageLimit` → `payout = payable`, Begründung *„Payout of {payout}"*.
- Ist `payable > coverageLimit` → `payout = coverageLimit` (**Capping**), Begründung *„Payout capped at coverage limit of {payout}"*.

> **Designentscheidung:** Die Deckelung ist eine *Berechnung*, keine *Validierung*.
> Sie lehnt den Anspruch nicht ab, sondern begrenzt nur den Betrag – und wird daher
> zentral in der Auszahlungsberechnung durchgeführt, nicht als Filter.

---

## Architektur

Die Bewertungslogik nutzt das **Chain-of-Responsibility-Pattern**:

```
                ClaimRequestDto
                       │ (MapStruct)
                       ▼
                 Claim + Policy
                       │
                       ▼
            ClaimEvaluationService
                       │
                       ▼
   ┌────────────────────────────────────────┐
   │              Filterkette                │
   │            (Short-Circuit)              │
   ├────────────────────────────────────────┤
   │  1. PolicyPeriodFilter   (@Order 1)     │ ─ invalid ─┐
   │  2. IncidentTypeFilter   (@Order 2)     │ ─ invalid ─┤
   │  3. DeductibleFilter     (@Order 3)     │ ─ invalid ─┤
   └───────────────────┬────────────────────┘             │
                       │ alle gültig                      │
                       ▼                                  ▼
             createFinalApproval()                    Ablehnung
              (Payout + Capping)                  (approved = false)
                       │                                  │
                       └────────────────┬─────────────────┘
                                        ▼
                                  ClaimDecision
                                        │ (MapStruct)
                                        ▼
                                  ClaimResponseDto
```

- **Filter** sind Spring-Beans (`@Component` + `@Order`) und werden per **Dependency Injection**
  als `List<ClaimFilter>` in den Service injiziert – die Reihenfolge ist über `@Order` garantiert.
- Neue Regeln lassen sich durch Hinzufügen eines weiteren `ClaimFilter`-Beans ergänzen,
  ohne den Service zu ändern (Open/Closed-Prinzip).
- **BigDecimal** wird durchgängig für Geldbeträge verwendet (keine Rundungsfehler).
- Domain-Objekte (`Claim`, `Policy`, `ClaimDecision`) sind **immutable Java Records**.

### Projektstruktur

```
src/main/java/org/example/claimsevaluation/
├── ClaimsEvaluationApplication.java   # Spring-Boot-Einstiegspunkt
├── controller/                        # REST-Endpoint
├── domain/                            # Claim, Policy, ClaimDecision (Records)
├── dto/                               # ClaimRequestDto, ClaimResponseDto
├── filter/                            # ClaimFilter + Regel-Implementierungen, FilterResult
├── mapper/                            # ClaimMapper (MapStruct)
├── service/                           # ClaimEvaluationService (Filterkette)
└── exception/                         # GlobalExceptionHandler
```

---

## Technologie-Stack

- **Java 21**, **Spring Boot 4.1.1** (Spring Web, Bean Validation)
- **MapStruct** – typsicheres DTO ↔ Domain Mapping (Compile-Zeit-generiert)
- **Lombok** – Boilerplate-Reduktion (Getter, Konstruktoren, Logging)
- **Jackson 3** – JSON-(De-)Serialisierung (Datumswerte als ISO-8601)
- **JUnit 5 + AssertJ** – Tests
- **Maven** (mit Wrapper `./mvnw`)

---

## API

### `POST /api/claims/evaluate`

Bewertet einen Anspruch gegen eine Police.

**Request Body** (`application/json`):

```json
{
  "incidentDate": "2025-09-15",
  "incidentType": "fire",
  "amount": 500.00,
  "policyStartDate": "2024-01-01",
  "policyEndDate": "2026-12-31",
  "coveredIncidentTypes": ["fire", "theft", "accident"],
  "deductible": 100.00,
  "coverageLimit": 1000.00
}
```

**Response** – `200 OK` (genehmigt):

```json
{
  "approved": true,
  "payout": 400.00,
  "reason": "Payout of 400.00"
}
```

**Response** – `200 OK` (abgelehnt):

```json
{
  "approved": false,
  "reason": "Incident type 'flood' is not covered by this policy"
}
```

> Bei Ablehnung ist `payout` `null` und wird dank `spring.jackson.default-property-inclusion=non-null`
> nicht serialisiert.

**Response** – `400 Bad Request` (Validierungs-/Formatfehler):

```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "amount: amount muss > 0 sein; incidentType: incidentType darf nicht leer sein",
  "status": 400,
  "timestamp": "2026-09-19T15:30:00Z"
}
```

### Feld-Validierung (`ClaimRequestDto`)

| Feld | Regel |
|------|-------|
| `incidentDate`, `policyStartDate`, `policyEndDate` | Pflicht, ISO-8601 (`YYYY-MM-DD`) |
| `incidentType` | Pflicht, nicht leer |
| `amount`, `coverageLimit` | Pflicht, `> 0` |
| `deductible` | Pflicht, `≥ 0` |
| `coveredIncidentTypes` | Pflicht, nicht leer |

---

## Build & Ausführung

```bash
# Bauen und Tests ausführen
./mvnw clean test

# Anwendung starten (Port 8080)
./mvnw spring-boot:run
```

---

## Tests

- **Unit-Tests**: `ClaimEvaluationServiceTest` (inkl. Capping & Short-Circuit),
  `PolicyPeriodFilterTest`, `IncidentTypeFilterTest`, `DeductibleFilterTest`.
- **Integrationstest**: `ClaimEvaluationControllerIT` (Bean-Validation der DTOs).
- **Manuelle Requests**: `src/test/http/claims-evaluation.http` – direkt aus IntelliJ
  ausführbare Requests inkl. Grenzfälle und Response-Assertions
  (Umgebungen in `http-client.env.json`).

```bash
./mvnw test
```

---

## Erweiterung um neue Regeln

1. Eine Klasse anlegen, die `ClaimFilter` implementiert.
2. Mit `@Component` und `@Order(n)` annotieren (bestimmt die Position in der Kette).
3. Fertig – Spring injiziert den Filter automatisch in den `ClaimEvaluationService`.


