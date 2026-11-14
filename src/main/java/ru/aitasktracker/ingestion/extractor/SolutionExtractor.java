package ru.aitasktracker.ingestion.extractor;

import java.util.List;
import java.util.Optional;

import ru.aitasktracker.ingestion.domain.IngestedSession;

/**
 * Extracts solution suggestions from a parsed session (contracts §6 / R-004).
 *
 * <p>This is a <strong>seam</strong> so that chunk 5 can plug in an
 * Ollama-based extractor without touching ingestion. The ingestion pipeline
 * <strong>never blocks</strong> on this extractor (R-028): any failure or
 * unavailability is treated as "no solutions this cycle" and ingestion of
 * tasks/steps/token_usage continues.
 *
 * <p>The provided {@link NoOpSolutionExtractor} does nothing — a safe default
 * that keeps ingestion independent of any external LLM.
 */
public interface SolutionExtractor {

    /**
     * Attempt to extract solution suggestions for one session.
     *
     * @param session a parsed opencode session (steps + structured solutions)
     * @return the suggested solutions in chronological (step) order, or
     *         {@link Optional#empty()} when extraction is skipped or unavailable
     */
    Optional<List<IngestedSession.IngestedSolution>> extract(IngestedSession session);

    /**
     * Whether this extractor is currently available (e.g. LLM reachable). Chunk 5
     * overrides this to report Ollama health. Chunk 3's no-op is always available
     * so ingestion degrades cleanly when no LLM extractor is configured.
     */
    default boolean isAvailable() {
        return true;
    }
}
