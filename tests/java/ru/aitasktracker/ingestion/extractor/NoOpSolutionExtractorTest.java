package ru.aitasktracker.ingestion.extractor;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import ru.aitasktracker.ingestion.domain.IngestedSession;

/**
 * Verifies the no-op extractor (R-028) returns an empty result regardless of
 * input, so ingestion runs identically whether or not an LLM extractor is
 * present. The implementation ignores its argument, so {@code null} is safe.
 */
class NoOpSolutionExtractorTest {

    @Test
    void extractReturnsEmpty() {
        Optional<List<IngestedSession.IngestedSolution>> result =
                new NoOpSolutionExtractor().extract(null);

        assertFalse(result.isPresent(), "no-op extractor must return empty optional");
    }
}
