package ru.aitasktracker.api.search.exception;

import org.springframework.http.HttpStatus;

import ru.aitasktracker.api.exception.ApiException;

/**
 * Empty/blank search query (contracts §4.4): 400 {@code INVALID_QUERY} with the
 * RU message «Запрос не может быть пустым».
 */
public class EmptyQueryException extends ApiException {

    private static final long serialVersionUID = 1L;

    public static final String CODE = "INVALID_QUERY";
    public static final String MESSAGE = "Введите запрос.";

    public EmptyQueryException(String message) {
        super(message != null && !message.isBlank() ? message : MESSAGE);
    }

    public EmptyQueryException() {
        this(MESSAGE);
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
