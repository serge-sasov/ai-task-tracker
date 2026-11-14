package ru.aitasktracker.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Base type for domain exceptions that map directly onto the RU error envelope
 * (contracts §5). Carries the stable machine {@code code}, the Russian
 * user-facing {@code message}, and the HTTP status. Kept in the {@code api}
 * package so it never leaks into the domain/ingestion layers.
 */
public abstract class ApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ApiException(String message) {
        super(message);
    }

    public abstract HttpStatus status();

    public abstract String code();
}
