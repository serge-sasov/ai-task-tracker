package ru.aitasktracker.api.web;

import java.net.http.HttpClient;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ru.aitasktracker.api.service.OllamaChecker;
import ru.aitasktracker.api.service.TaskQueryService;
import ru.aitasktracker.config.TrackerProperties;
import ru.aitasktracker.domain.repository.StepRepository;
import ru.aitasktracker.domain.repository.SolutionRepository;
import ru.aitasktracker.domain.repository.TaskRepository;
import ru.aitasktracker.domain.repository.TokenUsageRepository;

/**
 * Bean wiring for the {@code api} module (contracts section 4). Exposes the
 * non-managed collaborators: the {@link TrackerProperties}-driven Ollama HTTP
 * client, and the {@link ApiNoCacheFilter} that contributes the
 * {@code Cache-Control: no-store} header.
 *
 * <p>{@link HealthService}, {@link HistoryService} and the
 * {@code HealthController}/{@code HistoryController}/{@code TaskController}
 * controllers are Spring components ({@code @Service} / {@code @RestController})
 * discovered by component scanning the {@code api} package, so they must
 * <em>not</em> also be declared here (that would register them twice). Only the
 * plain (non-annotated) {@link TaskQueryService} is registered here as a
 * {@code @Bean}, alongside the {@link ApiNoCacheFilter} that contributes the
 * {@code Cache-Control: no-store} header and the {@link OllamaChecker}.
 */
@Configuration
public class ApiConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "apiNoCacheFilter")
    public ApiNoCacheFilter apiNoCacheFilter() {
        return new ApiNoCacheFilter();
    }

    @Bean
    public HttpClient ollamaHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofMillis(2000))
                .build();
    }

    @Bean
    public OllamaChecker ollamaChecker(TrackerProperties trackerProperties,
                                        HttpClient ollamaHttpClient) {
        return new OllamaChecker(trackerProperties.getOllama().getBaseUrl(), ollamaHttpClient);
    }

    @Bean
    public TaskQueryService taskQueryService(
            TaskRepository taskRepository,
            StepRepository stepRepository,
            SolutionRepository solutionRepository,
            TokenUsageRepository tokenUsageRepository) {
        return new TaskQueryService(taskRepository, stepRepository,
                solutionRepository, tokenUsageRepository);
    }
}
