package ru.aitasktracker.api;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.mockito.Mockito.mock;

import ru.aitasktracker.api.controller.HealthController;
import ru.aitasktracker.api.controller.HistoryController;
import ru.aitasktracker.api.controller.TaskController;
import ru.aitasktracker.api.service.HealthService;
import ru.aitasktracker.api.service.HistoryService;
import ru.aitasktracker.api.service.OllamaChecker;
import ru.aitasktracker.api.service.TaskQueryService;
import ru.aitasktracker.api.web.ApiConfiguration;
import ru.aitasktracker.config.IngestAutoConfiguration;
import ru.aitasktracker.config.TrackerAutoConfiguration;
import ru.aitasktracker.domain.repository.IngestStateRepository;
import ru.aitasktracker.domain.repository.StepRepository;
import ru.aitasktracker.domain.repository.SolutionRepository;
import ru.aitasktracker.domain.repository.TaskRepository;
import ru.aitasktracker.domain.repository.TokenUsageRepository;
import ru.aitasktracker.ingestion.lock.TaskLock;

/**
 * Context wiring test (defect B): {@link ApiConfiguration} and
 * {@link IngestAutoConfiguration} must not register the same bean twice (the
 * services/controllers are also picked up by the component scan of
 * {@code ru.aitasktracker.api}, and {@code TaskLock} has {@code @Component}
 * implementations plus a {@code @Bean}). With {@code setAllowBeanDefinitionOverriding(false)}
 * and {@code refresh()} succeeding, every bean resolves uniquely. Must fail
 * before Fix B and pass after.
 */
class ApiContextWiringTest {

    private AnnotationConfigApplicationContext context;

    private JdbcTemplate jdbc;
    private StringRedisTemplate redis;
    private RedisTemplate<String, Object> redisTemplate;
    private TaskRepository taskRepository;
    private StepRepository stepRepository;
    private SolutionRepository solutionRepository;
    private TokenUsageRepository tokenUsageRepository;
    private IngestStateRepository ingestStateRepository;

    @Configuration
    @ComponentScan(basePackages = {
            "ru.aitasktracker.api",
            "ru.aitasktracker.ingestion.lock"})
    static class ScanConfig {
    }

    @BeforeEach
    void setUp() {
        // Constructors are offline-safe; no socket/connection is opened.
        jdbc = new JdbcTemplate();
        redis = new StringRedisTemplate();
        redisTemplate = mock(RedisTemplate.class);
        taskRepository = mock(TaskRepository.class);
        stepRepository = mock(StepRepository.class);
        solutionRepository = mock(SolutionRepository.class);
        tokenUsageRepository = mock(TokenUsageRepository.class);
        ingestStateRepository = mock(IngestStateRepository.class);

        context = new AnnotationConfigApplicationContext();
        context.setAllowBeanDefinitionOverriding(false);
        context.register(ScanConfig.class);
        context.register(ApiConfiguration.class);
        context.register(IngestAutoConfiguration.class);
        context.register(TrackerAutoConfiguration.class);
        // Register the singletons the @Bean constructors resolve by type before
        // refresh. The repository mocks are not component-scanned (wrong base
        // package); register them directly in the bean factory. JdbcTemplate and
        // StringRedisTemplate are offline-safe singletons the wiring needs.
        context.getBeanFactory().registerSingleton("jdbcTemplate", jdbc);
        context.getBeanFactory().registerSingleton("stringRedisTemplate", redis);
        context.getBeanFactory().registerSingleton("redisTemplate", redisTemplate);
        context.getBeanFactory().registerSingleton("taskRepository", taskRepository);
        context.getBeanFactory().registerSingleton("stepRepository", stepRepository);
        context.getBeanFactory().registerSingleton("solutionRepository", solutionRepository);
        context.getBeanFactory().registerSingleton("tokenUsageRepository", tokenUsageRepository);
        context.getBeanFactory().registerSingleton("ingestStateRepository", ingestStateRepository);
    }

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void contextResolvesUniquelyWhenConfigApplied() {
        AnnotationConfigApplicationContext ctx = context;
        try {
            ctx.refresh();
        } catch (RuntimeException e) {
            fail("ApplicationContext failed to start (duplicate bean registration):\n"
                    + e.getMessage());
            return;
        }

        assertUnique(ctx, TaskLock.class, "TaskLock");
        assertUnique(ctx, HealthService.class, "HealthService");
        assertUnique(ctx, HistoryService.class, "HistoryService");
        assertUnique(ctx, TaskController.class, "TaskController");
        assertUnique(ctx, TaskQueryService.class, "TaskQueryService");
        assertUnique(ctx, OllamaChecker.class, "OllamaChecker");
    }

    private static void assertUnique(
            AnnotationConfigApplicationContext ctx, Class<?> type, String name) {
        Object[] beans = ctx.getBeansOfType(type).values().toArray(new Object[0]);
        assertArrayEquals(new Object[] { 1 }, new Object[] { beans.length },
                name + " must resolve to exactly one bean, found " + beans.length);
    }
}
