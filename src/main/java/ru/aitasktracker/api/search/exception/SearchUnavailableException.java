package ru.aitasktracker.api.search.exception;

import org.springframework.http.HttpStatus;

import ru.aitasktracker.api.exception.ApiException;

/**
 * Ollama down/timeout on {@code POST /api/search} (contracts §5, §6): 503
 * {@code AI_SEARCH_UNAVAILABLE} with the canonical RU envelope. This is a
 * controlled degradation, not an internal error, so it is deliberately never a
 * 500.
 */
public class SearchUnavailableException extends ApiException {

    private static final long serialVersionUID = 1L;

    public static final String CODE = "AI_SEARCH_UNAVAILABLE";
    public static final String MESSAGE =
            "AI-поиск недоступен: LLM не отвечает. Попробуйте позже.";

    public SearchUnavailableException(String message) {
        super(message != null && !message.isBlank() ? message : MESSAGE);
    }

    public SearchUnavailableException() {
        this(MESSAGE);
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
