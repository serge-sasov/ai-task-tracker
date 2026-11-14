package ru.aitasktracker.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Task detail: no row matches the requested {@code task_id} (contracts §4.3, §5).
 */
public class NotFound extends ApiException {

    private static final long serialVersionUID = 1L;

    public static final String CODE = "NOT_FOUND";
    public static final String MESSAGE = "Задача не найдена.";

    public NotFound(String message) {
        super(message != null && !message.isBlank()
                ? message
                : MESSAGE);
    }

    public NotFound() {
        this(MESSAGE);
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public String code() {
        return CODE;
    }
}
