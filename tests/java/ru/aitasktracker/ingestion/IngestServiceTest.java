package ru.aitasktracker.ingestion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import ru.aitasktracker.domain.TaskStatus;
import ru.aitasktracker.domain.entity.IngestStateEntity;
import ru.aitasktracker.domain.entity.StepEntity;
import ru.aitasktracker.domain.entity.TaskEntity;
import ru.aitasktracker.domain.repository.IngestStateRepository;
import ru.aitasktracker.domain.repository.StepRepository;
import ru.aitasktracker.domain.repository.SolutionRepository;
import ru.aitasktracker.domain.repository.TaskRepository;
import ru.aitasktracker.domain.repository.TokenUsageRepository;
import ru.aitasktracker.ingestion.domain.IngestedSession;
import ru.aitasktracker.ingestion.domain.IngestJsonParser;
import ru.aitasktracker.ingestion.domain.SourceStatus;
import ru.aitasktracker.ingestion.extractor.NoOpSolutionExtractor;
import ru.aitasktracker.ingestion.extractor.SolutionExtractor;

/**
 * Unit tests for {@link IngestService}. Pure unit tests — all repositories are
 * Mockito mocks, the parser/extractor are real, filesystem state is created under
 * {@link TempDir}. The pom has no H2/Testcontainers/Redis, so no Spring context.
 */
class IngestServiceTest {

    private final IngestStateRepository ingestStateRepository = mock(IngestStateRepository.class);
    private final TaskRepository taskRepository = mock(TaskRepository.class);
    private final StepRepository stepRepository = mock(StepRepository.class);
    private final SolutionRepository solutionRepository = mock(SolutionRepository.class);
    private final TokenUsageRepository tokenUsageRepository = mock(TokenUsageRepository.class);

    @Test
    void ingestAllAvailableUpdatesWatermark(@TempDir Path dir) throws IOException {
        writeSession(dir, "good-0000.json",
                "{\"session_id\":\"SID-1\",\"task_id\":\"TASK-1\",\"title\":\"Task 1\","
                        + "\"started_at\":\"2026-11-10T09:00:00\","
                        + "\"finished_at\":\"2026-11-10T10:00:00\","
                        + "\"actions\":[{\"at\":\"2026-11-10T09:00:01\",\"text\":\"a1\"},"
                        + "{\"at\":\"2026-11-10T09:00:02\",\"text\":\"a2\"}]}");

        // The new-task bug fix: save() must be called and the saved entity has a
        // non-null session_id. Stub save to return the passed-in entity.
        ArgumentCaptor<TaskEntity> taskCaptor =
                ArgumentCaptor.forClass(TaskEntity.class);
        when(taskRepository.save(any(TaskEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        IngestService service = service(dir);

        List<IngestedSession> sessions = service.ingestAll(60);

        assertEquals(1, sessions.size());
        verify(taskRepository, times(1)).save(taskCaptor.capture());
        assertEquals("SID-1", taskCaptor.getValue().getSessionId());
        verify(stepRepository, times(2)).save(any(StepEntity.class));
        verify(ingestStateRepository, atLeastOnce())
                .save(any(IngestStateEntity.class));
        // The watermark's source_status key recorded the (AVAILABLE) status.
        verify(ingestStateRepository).findByKey(IngestService.WK_SOURCE_STATUS);
    }

    @Test
    void ingestAllUnavailableReturnsEmptyAndRecordsStatus(@TempDir Path dir) {
        Path missing = dir.resolve("does-not-exist");

        IngestService service = service(missing);

        List<IngestedSession> sessions = service.ingestAll(60);

        assertTrue(sessions.isEmpty());
        verify(ingestStateRepository, atLeastOnce())
                .save(any(IngestStateEntity.class));
        verify(ingestStateRepository).findByKey(IngestService.WK_SOURCE_STATUS);
    }

    @Test
    void ingestAllCapturesPerFileErrorAndContinues(@TempDir Path dir) throws IOException {
        writeSession(dir, "good-0000.json",
                "{\"session_id\":\"SID-1\",\"task_id\":\"TASK-1\",\"title\":\"Task 1\"}");
        Files.writeString(dir.resolve("bad-0000.json"), "{ not valid json");

        IngestService service = service(dir);

        List<IngestedSession> sessions = service.ingestAll(60);

        // The one good file is ingested; the broken one is not fatal.
        assertEquals(1, sessions.size());

        // A parse watermark is written recording the bad file. Capture it and
        // assert the bad file name is recorded under the parse_errors key.
        ArgumentCaptor<IngestStateEntity> captor =
                ArgumentCaptor.forClass(IngestStateEntity.class);
        verify(ingestStateRepository, atLeastOnce())
                .save(captor.capture());
        assertTrue(captor.getAllValues().stream().anyMatch(
                e -> IngestService.WK_PARSE.equals(e.getKey())
                        && e.getValue().contains("bad-0000.json")));
        verify(ingestStateRepository, atLeastOnce())
                .save(any(IngestStateEntity.class));
    }

    @Test
    void upsertIsIdempotent(@TempDir Path dir) throws IOException {
        writeSession(dir, "good-0000.json",
                "{\"session_id\":\"SID-1\",\"task_id\":\"TASK-1\",\"title\":\"Task 1\"}");

        TaskEntity existing = new TaskEntity();
        existing.setTaskId("TASK-1");
        existing.setSessionId("SID-1");
        existing.setName("Task 1");
        existing.setStatus(TaskStatus.NOT_COMPLETED);
        existing.setStartedAt(LocalDateTime.of(2026, 11, 10, 9, 0));

        when(taskRepository.findBySessionId("SID-1")).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(TaskEntity.class))).thenAnswer(
                invocation -> invocation.getArgument(0));

        IngestService service = service(dir);

        List<IngestedSession> sessions = service.ingestAll(60);

        assertEquals(1, sessions.size());
        // Saved entity is the existing one, id preserved (no new task created).
        verify(taskRepository, times(1)).save(any(TaskEntity.class));
        assertEquals("SID-1", existing.getSessionId());
        assertEquals("TASK-1", existing.getTaskId());
    }

    @Test
    void computeSourceStatusUnavailable() {
        Path missing = Path.of("this-dir-does-not-exist-xyz");
        IngestService service = service(missing);

        assertEquals(SourceStatus.UNAVAILABLE, service.computeSourceStatus(60_000L));
    }

    @Test
    void computeSourceStatusStale(@TempDir Path dir) throws IOException {
        writeSession(dir, "good-0000.json",
                "{\"session_id\":\"SID-1\",\"task_id\":\"TASK-1\",\"title\":\"Task 1\"}");
        Path file = dir.resolve("good-0000.json");
        // ~10 minutes in the past; threshold = 3 * 60s = 180s.
        Files.setLastModifiedTime(file,
                java.nio.file.attribute.FileTime.fromMillis(
                        System.currentTimeMillis() - 10L * 60 * 1000));

        IngestService service = service(dir);

        assertEquals(SourceStatus.STALE, service.computeSourceStatus(60_000L));
    }

    @Test
    void computeSourceStatusAvailable(@TempDir Path dir) throws IOException {
        writeSession(dir, "good-0000.json",
                "{\"session_id\":\"SID-1\",\"task_id\":\"TASK-1\",\"title\":\"Task 1\"}");

        IngestService service = service(dir);

        assertEquals(SourceStatus.AVAILABLE, service.computeSourceStatus(60_000L));
    }

    @Test
    void ingestionNeverBlocksOnExtractor(@TempDir Path dir) throws IOException {
        writeSession(dir, "good-0000.json",
                "{\"session_id\":\"SID-1\",\"task_id\":\"TASK-1\",\"title\":\"Task 1\"}");

        SolutionExtractor throwingExtractor = new SolutionExtractor() {
            @Override
            public Optional<List<IngestedSession.IngestedSolution>> extract(
                    IngestedSession session) {
                throw new RuntimeException("extractor exploded");
            }
        };

        IngestService service = new IngestService(
                dir, new IngestJsonParser(), throwingExtractor,
                ingestStateRepository, taskRepository, stepRepository,
                solutionRepository, tokenUsageRepository);

        List<IngestedSession> sessions = service.ingestAll(60);

        // Extraction failure is swallowed; the session is still ingested.
        assertEquals(1, sessions.size());
        verify(ingestStateRepository, atLeastOnce())
                .save(any(IngestStateEntity.class));
    }

    private IngestService service(Path dir) {
        return new IngestService(
                dir, new IngestJsonParser(), new NoOpSolutionExtractor(),
                ingestStateRepository, taskRepository, stepRepository,
                solutionRepository, tokenUsageRepository);
    }

    private void writeSession(Path dir, String name, String json) throws IOException {
        Files.writeString(dir.resolve(name), json);
    }
}
