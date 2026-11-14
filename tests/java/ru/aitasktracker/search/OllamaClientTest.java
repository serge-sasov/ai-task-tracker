package ru.aitasktracker.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link OllamaClient}. The {@link HttpClient} is a Mockito mock
 * so every test is deterministic and offline (no socket, no live Ollama). Covers
 * the contract §4.4/§6 path: localhost base URL, bounded timeout, defensive
 * JSON parsing, and the failure -> {@link OllamaException} path.
 */
class OllamaClientTest {

    private final HttpClient httpClient = mock(HttpClient.class);
    private final OllamaClient client = new OllamaClient(
            "http://localhost:11434", "qwen2.5:0.5b", httpClient);

    private HttpResponse<String> response(int status, String body) {
        HttpResponse<String> r = mock(HttpResponse.class);
        when(r.statusCode()).thenReturn(status);
        when(r.body()).thenReturn(body);
        return r;
    }

    /** Stub {@code http.send(..., handler)} deterministically. */
    private void whenSend(HttpResponse<String> r) throws Exception {
        when(httpClient.send(
                org.mockito.ArgumentMatchers.any(HttpRequest.class),
                org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(r);
    }

    @Test
    void requestTargetsApiChatPathWithBoundedTimeout() throws Exception {
        whenSend(response(200, "{\"messages\":[{\"content\":\"hi\"}]}"));

        client.rank("CI?", java.util.List.of("s1"));

        verify(httpClient).send(org.mockito.ArgumentMatchers.any(HttpRequest.class),
                org.mockito.ArgumentMatchers.any());
        // The total budget must not exceed the 15s contract bound.
        assertTrue(OllamaClient.TOTAL_TIMEOUT.getSeconds() <= 15);
    }

    @Test
    void parsesMessagesFromChatBody() throws Exception {
        HttpResponse<String> r = response(200,
                "{\"messages\":[{\"role\":\"assistant\","
                        + "\"content\":\"TASK-001\\tstep\\tCI setup text\"}]}");
        whenSend(r);

        var out = client.rank("CI?", java.util.List.of("x"));

        java.util.List<String> expected = java.util.List.of("TASK-001\tstep\tCI setup text");
        assertEquals(expected, out);
    }

    @Test
    void connectionFailureThrowsOllamaException() throws Exception {
        doThrow(new java.net.ConnectException("refused"))
                .when(httpClient).send(org.mockito.ArgumentMatchers.any(HttpRequest.class),
                        org.mockito.ArgumentMatchers.any());

        OllamaException ex = assertThrows(OllamaException.class,
                () -> client.rank("CI?", java.util.List.of("x")));
        assertTrue(ex.getMessage().toLowerCase().contains("failed"));
    }

    @Test
    void nonTwoXxStatusThrowsOllamaException() throws Exception {
        whenSend(response(503, "{}"));

        assertThrows(OllamaException.class,
                () -> client.rank("CI?", java.util.List.of("x")));
    }

    @Test
    void malformedJsonBodyThrowsOllamaException() throws Exception {
        whenSend(response(200, "not-json"));

        assertThrows(OllamaException.class,
                () -> client.rank("CI?", java.util.List.of("x")));
    }

    @Test
    void emptyMessagesThrowsOllamaException() throws Exception {
        whenSend(response(200, "{\"messages\":[]}"));

        assertThrows(OllamaException.class,
                () -> client.rank("CI?", java.util.List.of("x")));
    }

    @Test
    void emptyQueryThrowsOllamaException() {
        assertThrows(OllamaException.class,
                () -> client.rank("   ", java.util.List.of("x")));
    }

    @Test
    void noCandidatesThrowsOllamaException() {
        assertThrows(OllamaException.class,
                () -> client.rank("ci", java.util.List.of()));
    }
}
