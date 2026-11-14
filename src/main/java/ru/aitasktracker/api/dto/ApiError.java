package ru.aitasktracker.api.dto;

/**
 * Error envelope shared by every non-2xx response (contracts §5). Exactly two
 * fields, no stack trace, no extras.
 */
public class ApiError {

    private final String code;
    private final String message;

    public ApiError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
