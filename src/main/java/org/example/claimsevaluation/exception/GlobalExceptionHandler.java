package org.example.claimsevaluation.exception;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Behandelt Jakarta Validation Fehler.
     * Wird geworfen, wenn @Valid fehlschlägt (ungültige Eingabe).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        if (errors.isEmpty()) {
            errors = "Validierungsfehler";
        }

        log.warn("Validation error: {}", errors);

        return ResponseEntity.badRequest()
                .body(new ErrorResponse("VALIDATION_ERROR", errors, HttpStatus.BAD_REQUEST.value()));
    }


    /**
     * Catch-All für unerwartete Exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {

        log.error("Unexpected exception: {}", ex.getClass().getName(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        "INTERNAL_ERROR",
                        "Interner Serverfehler",
                        HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }

    @Data
    public static class ErrorResponse {
        private String errorCode;
        private String message;
        private int status;
        private Instant timestamp;

        public ErrorResponse(String errorCode, String message, int status) {
            this.errorCode = errorCode;
            this.message = message;
            this.status = status;
            this.timestamp = Instant.now();
        }
    }
}
