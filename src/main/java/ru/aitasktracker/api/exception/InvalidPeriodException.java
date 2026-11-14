package ru.aitasktracker.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Invalid period: {@code from}/{@code to} (or {@code date}) that does not parse as
 * ISO 8601 local wall-clock (contracts §4.2/4.5/4.6, §5).
 */
public class InvalidPeriodException extends ApiException {

    private static final long serialVersionUID = 1L;

    public static final String CODE = "INVALID_PERIOD";
    public static final String MESSAGE = "Некорректный период. Проверьте даты.";

    public InvalidPeriodException(String message) {
        super(message != null && !message.isBlank()
                ? message
                : MESSAGE);
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String code() {
        return CODE;
    }
}
