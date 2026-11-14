package ru.aitasktracker.api.exception;

import org.springframework.http.HttpStatus;

/**
 * DELETE /api/history received no body, no {@code confirm} field, or a value other
 * than {@code true} (contracts §4.7, §5).
 */
public class ConfirmationRequiredException extends ApiException {

    private static final long serialVersionUID = 1L;

    public static final String CODE = "CONFIRMATION_REQUIRED";
    public static final String MESSAGE = "Требуется подтверждение удаления.";

    public ConfirmationRequiredException(String message) {
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
