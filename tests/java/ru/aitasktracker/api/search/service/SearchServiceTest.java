package ru.aitasktracker.api.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ru.aitasktracker.api.dto.SearchResponse;
import ru.aitasktracker.api.search.exception.EmptyQueryException;
import ru.aitasktracker.api.search.exception.SearchUnavailableException;
import ru.aitasktracker.domain.entity.StepEntity;
import ru.aitasktracker.domain.entity.SolutionEntity;
import ru.aitasktracker.domain.repository.StepRepository;
import ru.aitasktracker.domain.repository.SolutionRepository;
import ru.aitasktracker.search.OllamaClient;
import ru.aitasktracker.search.OllamaException;

/**
 * Unit tests for {@link SearchService}. All collaborators are Mockito mocks; no
 * live Ollama or database (the pom has neither). Covers the contract §4.4/§5
 * paths: empty query -> 400, Ollama failure -> 503 {@code AI_SEARCH_UNAVAILABLE},
 * top-K, and the honest-empty-result path.
 */
class SearchServiceTest {

    private StepRepository stepRepository;
    private SolutionRepository solutionRepository;
    private OllamaClient ollamaClient;
    private SearchService service;

    @BeforeEach
    void setUp() {
        stepRepository = mock(StepRepository.class);
        solutionRepository = mock(SolutionRepository.class);
        ollamaClient = mock(OllamaClient.class);
        service = new SearchService(stepRepository, solutionRepository, ollamaClient, 5);
    }

    @Test
    void emptyQueryThrowsInvalidQuery() {
        EmptyQueryException ex = assertThrows(EmptyQueryException.class,
                () -> service.search("", 5, null));
        assertEquals(EmptyQueryException.CODE, ex.code());
        verify(ollamaClient, never()).rank(any(), any());
    }

    @Test
    void blankQueryThrowsInvalidQuery() {
        assertThrows(EmptyQueryException.class,
                () -> service.search("   ", 5, null));
    }

    @Test
    void ollamaFailureReturnsSearchUnavailable() {
        when(stepRepository.findByTaskIdOrderByAtAsc(any()))
                .thenReturn(List.of(new StepEntity("TASK-001", "m1", null, "CI text")));
        when(ollamaClient.rank(any(), any()))
                .thenThrow(new OllamaException("timeout"));

        SearchUnavailableException ex = assertThrows(SearchUnavailableException.class,
                () -> service.search("CI", 5, null));
        assertEquals(SearchUnavailableException.CODE, ex.code());
    }

    @Test
    void noCandidatesReturnsEmptyResult() {
        when(stepRepository.findByTaskIdOrderByAtAsc(any()))
                .thenReturn(List.of());
        when(solutionRepository.findByTaskIdOrderBySolutionIdAsc(any()))
                .thenReturn(List.of());

        SearchResponse resp = service.search("CI", 5, null);

        assertEquals(0, resp.getTotal());
        assertTrue(resp.getResults().isEmpty());
        verify(ollamaClient, never()).rank(any(), any());
    }

    @Test
    void ranksAndTopKSelection() {
        when(stepRepository.findByTaskIdOrderByAtAsc(any()))
                .thenReturn(List.of(new StepEntity("TASK-001", "m1", null, "CI text 1"),
                        new StepEntity("TASK-001", "m2", null, "CI text 2"),
                        new StepEntity("TASK-001", "m3", null, "CI text 3")));

        // Simulate the model returning 3 ranked candidates.
        List<String> ranked = List.of(
                "TASK-001\tstep\tCI text 3",
                "TASK-001\tstep\tCI text 1",
                "TASK-001\tstep\tCI text 2");
        when(ollamaClient.rank(any(), any())).thenReturn(ranked);

        SearchResponse resp = service.search("CI", 5, null);

        assertEquals(3, resp.getTotal());
        // Scores must be strictly descending.
        assertEquals(resp.getResults().get(0).getScore(),
                SearchService.scoreForRank(0));
        assertEquals(resp.getResults().get(1).getScore(),
                SearchService.scoreForRank(1));
        assertTrue(resp.getResults().get(0).getScore()
                > resp.getResults().get(1).getScore());
    }

    @Test
    void respectsTopK() {
        when(stepRepository.findByTaskIdOrderByAtAsc(any()))
                .thenReturn(List.of(
                        new StepEntity("TASK-001", "m1", null, "CI text 1"),
                        new StepEntity("TASK-002", "m2", null, "CI text 2")));
        List<String> ranked = List.of(
                "TASK-001\tstep\tCI text 1",
                "TASK-002\tstep\tCI text 2");
        when(ollamaClient.rank(any(), any())).thenReturn(ranked);

        SearchResponse resp = service.search("CI", 1, null);

        assertEquals(1, resp.getTotal());
    }
}
