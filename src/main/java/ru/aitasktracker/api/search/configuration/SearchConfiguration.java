package ru.aitasktracker.api.search.configuration;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ru.aitasktracker.search.OllamaClient;

/**
 * Spring wiring for the semantic search feature (contracts §6). Defines the
 * {@link OllamaClient} as a {@code @Bean} rather than a {@code @Component} so its
 * lifetime and the underlying {@link HttpClient} are controlled here, not by the
 * component scanner. This class lives under {@code api.search} so it is picked up
 * by the {@code api} component scan used across the application's wiring tests.
 *
 * <p>All properties default to the local Ollama endpoint. In unit wiring tests
 * the {@code @Value} falls back to the defaults; in production
 * {@code application.yml} can override them.
 */
@Configuration
public class SearchConfiguration {

    private static final Duration CONNECT_TIMEOUT = Duration.ofMillis(3_000L);

    @Bean
    public OllamaClient ollamaClient(
            @Value("${api.ollama-base-url:http://localhost:11434}") String baseUrl,
            @Value("${api.ollama-model:qwen2.5:0.5b}") String model) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
        return new OllamaClient(baseUrl, model, httpClient);
    }
}
