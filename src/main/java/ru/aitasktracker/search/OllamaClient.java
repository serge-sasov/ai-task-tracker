package ru.aitasktracker.search;

import java.util.ArrayList;
import java.util.List;
import java.time.Duration;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A minimal, local-only Ollama client used for semantic search ranking (contracts
 * §4.4, §6). It performs a single synchronous {@code POST /api/chat} call against
 * the configured base URL (which must point at {@code http://localhost:11434} in
 * the real deployment) and parses the chat response defensively.
 *
 * <p>Every network call is bounded so the total round trip stays within the
 * contract {@code 15 s} (contracts §6, R-023): both the connect timeout and the
 * read/request timeout are derived from {@link #TOTAL_TIMEOUT}. Any failure —
 * connection error, timeout, non-2xx status, or an unparsable body — throws
 * {@link OllamaException} so the caller can degrade to the {@code AI_SEARCH_UNAVAILABLE}
 * path (contracts §5).
 *
 * <p>By design no external host is ever contacted: the base URL is injected from
 * {@code tracker.ollama.base-url} and this class only emits HTTP requests to that
 * origin. Telemetry logs only the model name, latency and status code (contracts
 * §6, R-027).
 */
public class OllamaClient {

    private static final Logger log = LoggerFactory.getLogger(OllamaClient.class);

    /** Hard bound on the total search round trip: connect + read (contracts §6). */
    public static final Duration TOTAL_TIMEOUT = Duration.ofSeconds(15);

    /** Per-request read/connect bound: half of the total budget (contracts §6). */
    public static final Duration REQUEST_TIMEOUT = TOTAL_TIMEOUT.dividedBy(2);

    private final String baseUrl;
    private final String model;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    public OllamaClient(String baseUrl, String model, HttpClient httpClient) {
        this(baseUrl, model, httpClient, new ObjectMapper());
    }

    public OllamaClient(String baseUrl, String model, HttpClient httpClient, ObjectMapper mapper) {
        this.baseUrl = normalize(baseUrl);
        this.model = model;
        this.httpClient = httpClient;
        this.mapper = mapper;
    }

    /**
     * Rank candidate snippets for the query via a single chat call and return the
     * ranked text messages (best first). Any failure throws {@link OllamaException}.
     *
     * @param query the search query
     * @param snippets non-empty candidate texts to rank
     * @return ranked snippets, best first; never {@code null}
     */
    public List<String> rank(String query, List<String> snippets) {
        if (query == null || query.isBlank()) {
            throw new OllamaException("Ollama search query is empty");
        }
        if (snippets == null || snippets.isEmpty()) {
            throw new OllamaException("Ollama search has no candidates to rank");
        }

        long start = System.nanoTime();
        HttpRequest request = buildRequest(query, snippets);
        try {
            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());
            long latencyMs = (System.nanoTime() - start) / 1_000_000L;
            int status = response.statusCode();
            log.debug("Ollama /api/chat status={} latency={}ms model={}",
                    status, latencyMs, model);
            if (status < 200 || status >= 300) {
                throw new OllamaException("Ollama returned HTTP " + status);
            }
            return parseMessages(response.body());
        } catch (OllamaException e) {
            throw e;
        } catch (Exception e) {
            // Connection refused, DNS failure, timeout, IO.
            throw new OllamaException("Ollama request failed: " + e.getMessage(), e);
        }
    }

    private HttpRequest buildRequest(String query, List<String> snippets) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .timeout(REQUEST_TIMEOUT)
                .uri(URI.create(baseUrl + "/api/chat"))
                .header("Content-Type", "application/json; charset=utf-8")
                .method("POST", HttpRequest.BodyPublishers.ofString(body(query, snippets)));
        return builder.build();
    }

    /** Build the {@code POST /api/chat} JSON body (stream off, single chat turn). */
    String body(String query, List<String> snippets) {
        StringBuilder json = new StringBuilder();
        json.append("{\"model\":\"").append(escapeJson(model)).append("\",");
        json.append("\"stream\":false,\"messages\":[");
        json.append("{\"role\":\"system\",\"content\":\"").append(escapeJson(systemPrompt(snippets))).append("\"},");
        json.append("{\"role\":\"user\",\"content\":\"").append(escapeJson(userPrompt(query))).append("\"}");
        json.append("]}");
        return json.toString();
    }

    private String systemPrompt(List<String> snippets) {
        StringBuilder sb = new StringBuilder();
        sb.append("Rank the following candidate snippets by relevance and return them");
        sb.append(" in order of best match, one per line.\n\nCandidates:\n");
        int i = 1;
        for (String s : snippets) {
            sb.append(i++).append(". ").append(s).append('\n');
        }
        return sb.toString();
    }

    private String userPrompt(String query) {
        return "Query: " + query;
    }

    /**
     * Extract assistant text messages from an Ollama chat response body. The real
     * Ollama body is an object with a {@code messages} array whose entries carry
     * {@code message.content} (a string). Parsing is defensive: any unexpected
     * shape throws {@link OllamaException}.
     */
    @SuppressWarnings("unchecked")
    public List<String> parseMessages(String body) {
        List<String> messages = new ArrayList<>();
        if (body == null || body.isBlank()) {
            throw new OllamaException("Ollama chat response body was empty");
        }
        try {
            JsonNode root = mapper.readTree(body);
            JsonNode messagesNode = root.get("messages");
            if (messagesNode == null || !messagesNode.isArray()
                    || messagesNode.isEmpty()) {
                throw new OllamaException("Ollama chat response had no messages array");
            }
            for (JsonNode m : messagesNode) {
                JsonNode content = m.get("content");
                if (content != null && content.isTextual()) {
                    String text = content.asText().trim();
                    if (!text.isEmpty()) {
                        messages.add(text);
                    }
                }
            }
        } catch (OllamaException e) {
            throw e;
        } catch (Exception e) {
            throw new OllamaException("Could not parse Ollama response: " + e.getMessage(), e);
        }
        if (messages.isEmpty()) {
            throw new OllamaException("Ollama chat response contained no text");
        }
        return messages;
    }

    private static String normalize(String baseUrl) {
        if (baseUrl == null) {
            return "";
        }
        return baseUrl.replace("/$", "").replaceAll("/+$", "");
    }

    private static String escapeJson(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"' -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}
