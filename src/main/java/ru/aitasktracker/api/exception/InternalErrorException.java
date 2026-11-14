package ru.aitasktracker.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Unhandled internal failure (contracts §5): the controller advice converts any
 * unexpected exception into this response so no stack trace or internals leak.
 */
public class InternalErrorException extends ApiException {

    private static final long serialVersionUID = 1L;

    public static final String CODE = "INTERNAL_ERROR";
    public static final String MESSAGE = "Внутренняя ошибка. Повторите попытку.";

    public InternalErrorException(String message) {
        super(message != null && !message.isBlank()
                ? message
                : MESSAGE);
    }

    public InternalErrorException() {
        this(MESSAGE);
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    @Override
    public String code() {
        return CODE;
    }
}
