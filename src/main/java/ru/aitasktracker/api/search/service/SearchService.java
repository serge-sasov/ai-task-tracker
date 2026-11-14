package ru.aitasktracker.api.search.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ru.aitasktracker.api.dto.SearchHit;
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
 * Semantic search over task steps and solutions (contracts §4.4, API-003).
 *
 * <p>The service gathers candidate snippets from the {@code steps}/{@code
 * solutions} repositories, asks the {@link OllamaClient} to rank them, and
 * synthesises a ranked {@link SearchResponse}. Any Ollama failure becomes a
 * controlled {@link SearchUnavailableException} (503, never a 500); an empty or
 * blank query becomes {@link EmptyQueryException} (400).
 *
 * <p>{@code topK} is injected from {@code tracker.search-top-k} (default 5,
 * contracts §4.4).
 *
 * <p>Ranking format: every candidate is sent to Ollama as
 * {@code "<task_id>\t<kind>\t<original_text>"} so the ranked messages can be
 * parsed back into a {@link SearchHit}. The {@code score} is a deterministic
 * 0..1 value derived from rank position (highest first). No semantic similarity
 * or fabricated relevance text is produced; the snippets returned by the model
 * are what get ranked.
 */
@Service
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    private static final String DELIM = "\t";

    private final StepRepository stepRepository;
    private final SolutionRepository solutionRepository;
    private final OllamaClient ollamaClient;
    private final int topK;

    public SearchService(StepRepository stepRepository,
                         SolutionRepository solutionRepository,
                         OllamaClient ollamaClient,
                         @Value("${tracker.search-top-k:5}") int topK) {
        this.stepRepository = stepRepository;
        this.solutionRepository = solutionRepository;
        this.ollamaClient = ollamaClient;
        this.topK = topK;
    }

    public SearchResponse search(String query, Integer limit, String taskId) {
        if (query == null || query.isBlank()) {
            throw new EmptyQueryException(EmptyQueryException.MESSAGE);
        }
        int k = clampLimit(limit);
        List<Candidate> candidates = collectCandidates(taskId);
        if (candidates.isEmpty()) {
            // No data to rank: honest empty result, not an error (contracts §4.4).
            return new SearchResponse(query, 0, List.of());
        }

        List<String> ranked;
        try {
            List<String> payload = new ArrayList<>();
            for (Candidate c : candidates) {
                payload.add(c.format());
            }
            ranked = ollamaClient.rank(query, payload);
        } catch (OllamaException e) {
            log.info("AI search unavailable for query '{}': {}", query, e.getMessage());
            throw new SearchUnavailableException(SearchUnavailableException.MESSAGE);
        }

        List<SearchHit> hits = new ArrayList<>();
        int rank = 0;
        for (String message : ranked) {
            Candidate c = parse(message);
            if (c == null) {
                continue; // malformed model message; skip defensively
            }
            hits.add(new SearchHit(c.taskId(), c.kind(), c.text(), scoreForRank(rank)));
            rank++;
            if (rank >= k) {
                break;
            }
        }
        // Stable: scoreForRank is already descending, but sort explicitly so the
        // ordering is unambiguous even if the model returns fewer than k.
        hits.sort(Comparator.comparing(SearchHit::getScore).reversed()
                .thenComparing(Comparator.comparing(SearchHit::getTaskId)));
        return new SearchResponse(query, hits.size(), hits);
    }

    private int clampLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return topK;
        }
        return Math.min(limit, topK);
    }

    /** Collect non-empty snippets from steps (kind=step) and solutions (kind=solution). */
    List<Candidate> collectCandidates(String taskId) {
        List<Candidate> snippets = new ArrayList<>();
        for (StepEntity step : stepRepository.findByTaskIdOrderByAtAsc(taskId)) {
            String text = step.getText();
            if (text != null && !text.isBlank()) {
                snippets.add(new Candidate(taskId, "step", text));
            }
        }
        for (SolutionEntity solution : solutionRepository
                .findByTaskIdOrderBySolutionIdAsc(taskId)) {
            String text = solution.getText();
            if (text != null && !text.isBlank()) {
                snippets.add(new Candidate(taskId, "solution", text));
            }
        }
        return snippets;
    }

    private Candidate parse(String message) {
        if (message == null) {
            return null;
        }
        String[] parts = message.split(DELIM, 3);
        if (parts.length < 3) {
            return null;
        }
        String taskId = parts[0].trim();
        String kind = parts[1].trim();
        String text = parts[2].trim();
        if (taskId.isEmpty() || kind.isEmpty() || text.isEmpty()) {
            return null;
        }
        return new Candidate(taskId, kind, text);
    }

    /** Deterministic descending score 0..1 derived from rank position. */
    static float scoreForRank(int rank) {
        return Math.max(0.1f, 1.0f - rank * 0.05f);
    }

    /**
     * A single ranked candidate, tagged by source kind so the final hit can be
     * returned as {@code step} or {@code solution}. Serialized as
     * {@code task_id\tkind\ttext}.
     */
    private record Candidate(String taskId, String kind, String text) {
        String format() {
            return taskId + DELIM + kind + DELIM + text;
        }
    }
}
