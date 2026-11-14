package ru.aitasktracker.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import ru.aitasktracker.api.dto.ApiError;
import ru.aitasktracker.api.exception.ApiException;
import ru.aitasktracker.api.exception.InternalErrorException;

/**
 * Maps exceptions to the RU error envelope (contracts section 5). Any exception
 * that extends {@link ApiException} renders with its own status; everything else
 * becomes {@code 500 INTERNAL_ERROR} so no stack trace or internals ever leak.
 */
@RestControllerAdvice
public class ApiErrorAdapter {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(ApiException ex) {
        return new ResponseEntity<>(
                new ApiError(ex.code(), ex.getMessage()),
                ex.status());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        return new ResponseEntity<>(
                new ApiError(
                        InternalErrorException.CODE,
                        InternalErrorException.MESSAGE),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
