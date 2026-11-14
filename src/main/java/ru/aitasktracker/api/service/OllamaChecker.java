package ru.aitasktracker.api.service;

import java.time.Duration;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Minimal Ollama liveness check (contracts section 6 / health §4). Performs a
 * single GET against {@code <baseUrl>/api/tags} with a short timeout; any
 * response (even 404) means the server is {@code up}; a connection error or
 * timeout means {@code down}.
 *
 * <p>The base URL comes from {@code TrackerProperties.ollama.base-url}; the 2s
 * timeout bound is per contracts section 6 (the endpoint call is bounded, the
 * broader 15s search bound is not relevant here).
 */
public class OllamaChecker {

    private final String baseUrl;
    private final HttpClient httpClient;

    public OllamaChecker(String baseUrl, HttpClient httpClient) {
        this.baseUrl = baseUrl.replace("/$", "");
        this.httpClient = httpClient;
    }

    /** {@code true} if the Ollama /api/tags endpoint answered at all. */
    public boolean isUp() {
        HttpRequest request = HttpRequest.newBuilder()
                .timeout(Duration.ofMillis(2000))
                .uri(URI.create(baseUrl + "/api/tags"))
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
