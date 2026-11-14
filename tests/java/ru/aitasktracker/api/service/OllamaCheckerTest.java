package ru.aitasktracker.api.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Unit tests for {@link OllamaChecker}. Uses a Mockito-mocked {@link HttpClient}
 * so the check is fully deterministic and offline — no real socket. The Ollama
 * checker treats any HTTP response (even 404) as {@code up}; a connection error or
 * timeout as {@code down}.
 */
class OllamaCheckerTest {

    private final HttpClient httpClient = mock(HttpClient.class);

    private OllamaChecker checker(String baseUrl) {
        return new OllamaChecker(baseUrl, httpClient);
    }

    @Test
    void isUpWhenServerReturnsResponse() throws Exception {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        doReturn(response).when(httpClient).send(any(HttpRequest.class), any());

        Assertions.assertTrue(checker("http://localhost:11434").isUp());
    }

    @Test
    void isUpEvenWhenServerReturnsErrorStatus() throws Exception {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(404);
        doReturn(response).when(httpClient).send(any(HttpRequest.class), any());

        Assertions.assertTrue(checker("http://localhost:11434").isUp());
    }

    @Test
    void isDownWhenServerUnreachable() throws Exception {
        doThrow(new ConnectException("connection refused")).when(httpClient)
                .send(any(HttpRequest.class), any());

        Assertions.assertFalse(checker("http://localhost:11434").isUp());
    }

    @Test
    void requestTargetsApiTagsPath() throws Exception {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        doReturn(response).when(httpClient).send(any(HttpRequest.class), any());

        Assertions.assertTrue(checker("http://localhost:11434").isUp());

        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).send(captor.capture(), any());
        Assertions.assertEquals("http://localhost:11434/api/tags",
                captor.getValue().uri().toString());
    }

    @Test
    void trailingSlashInBaseUrlIsStripped() throws Exception {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        doReturn(response).when(httpClient).send(any(HttpRequest.class), any());

        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        Assertions.assertTrue(checker("http://localhost:11434/$").isUp());

        verify(httpClient, times(1)).send(captor.capture(), any());
        Assertions.assertEquals(URI.create("http://localhost:11434/api/tags"),
                captor.getValue().uri());
    }
}
