package ru.aitasktracker.search;

/**
 * Raised by {@link OllamaClient} on any failure to talk to the local Ollama
 * server: connection refused, timeout, or a malformed/unexpected response body.
 * The API layer translates this into the 503 {@code AI_SEARCH_UNAVAILABLE}
 * envelope (contracts §5).
 */
public class OllamaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public OllamaException(String message) {
        super(message);
    }

    public OllamaException(String message, Throwable cause) {
        super(message, cause);
    }
}
