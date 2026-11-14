package ru.aitasktracker.ingestion.extractor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import ru.aitasktracker.ingestion.domain.IngestedSession;

/**
 * Default {@link SolutionExtractor} that performs no extraction. Ingestion runs
 * identically whether or not an LLM extractor is present (R-028). Chunk 5
 * supplies an Ollama-based extractor implementing the same interface.
 */
public class NoOpSolutionExtractor implements SolutionExtractor {

    @Override
    public Optional<List<IngestedSession.IngestedSolution>> extract(IngestedSession session) {
        return Optional.empty();
    }
}
