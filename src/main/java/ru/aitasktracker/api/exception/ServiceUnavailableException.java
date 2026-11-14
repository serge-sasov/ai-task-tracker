package ru.aitasktracker.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Readiness failure on {@code GET /api/health}: PostgreSQL or Redis is down. The
 * body is still the error envelope; only the HTTP status is 503 (contracts section 4.1,
 * section 5).
 */
public class ServiceUnavailableException extends ApiException {

    private static final long serialVersionUID = 1L;

    public static final String CODE = "SERVICE_UNAVAILABLE";
    public static final String MESSAGE = "\u0421\u0435\u0440\u0432\u0438\u0441 \u043D\u0435\u0434\u043E\u0441\u0442\u0443\u043F\u0435\u043D.";

    public ServiceUnavailableException(String message) {
        super(message != null && !message.isBlank()
                ? message
                : MESSAGE);
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.SERVICE_UNAVAILABLE;
    }

    @Override
    public String code() {
        return CODE;
    }
}
