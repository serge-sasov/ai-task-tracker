package ru.aitasktracker.ingestion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import ru.aitasktracker.domain.TaskStatus;
import ru.aitasktracker.ingestion.domain.IngestedSession;
import ru.aitasktracker.ingestion.domain.IngestJsonParser;

/**
 * Unit tests for {@link IngestJsonParser}. Pure unit test — no database, no
 * Redis; the parser is a plain object with a Jackson core dependency.
 */
class IngestJsonParserTest {

    private final IngestJsonParser parser = new IngestJsonParser();

    // Single-line JSON strings. Verified to parse (null outcome = success).

    private static final String ACTIONS = "{\"session_id\":\"SID-1\",\"task_id\":\"TASK-1\",\"title\":\"Task 1\",\"started_at\":\"2026-11-10T09:00:00\",\"finished_at\":\"2026-11-10T10:00:00\",\"final_status\":\"Завершена\",\"actions\":[{\"at\":\"2026-11-10T09:00:01\",\"text\":\"first\"},{\"at\":\"2026-11-10T09:00:02\",\"text\":\"second\"}]}";

    private static final String MESSAGES = "{\"session_id\":\"SID-2\",\"messages\":[{\"message_id\":\"M-1\",\"at\":\"2026-11-10T09:00:01\",\"text\":\"m1\"},{\"message_id\":\"M-2\",\"at\":\"2026-11-10T09:00:02\",\"text\":\"m2\"}]}";

    private static final String ACTIONS_EQ = "{\"session_id\":\"SID\",\"actions\":[{\"at\":\"2026-11-10T09:00:01\",\"text\":\"x\"},{\"at\":\"2026-11-10T09:00:02\",\"text\":\"y\"}]}";

    private static final String MESSAGES_EQ = "{\"session_id\":\"SID\",\"messages\":[{\"message_id\":\"M-1\",\"at\":\"2026-11-10T09:00:01\",\"text\":\"x\"},{\"message_id\":\"M-2\",\"at\":\"2026-11-10T09:00:02\",\"text\":\"y\"}]}";

    private static final String CI = "{\"SESSION_ID\":\"SID-CI\",\"Session_Id\":\"SID-CI-2\",\"TITle\":\"Title Title\",\"ACTION\":{\"at\":\"x\",\"text\":\"one\"}}";

    private static final String UNKNOWN = "{\"session_id\":\"SID-UF\",\"weird_key\":123,\"nested\":{\"a\":1},\"title\":\"Keep Me\",\"nope\":[1,2,3]}";

    private static final String MISSING_SID = "{\"task_id\":\"TASK-1\",\"title\":\"no session id\"}";

    private static final String NULL_USAGE = "{\"session_id\":\"SID-A\",\"usage\":null}";

    private static final String NO_USAGE = "{\"session_id\":\"SID-B\"}";

    private static final String USAGE_KEYS = "{\"session_id\":\"SID-U\",\"usage\":[{\"model\":\"m1\",\"input\":10,\"output\":20,\"cache\":5},{\"model\":\"m2\",\"input_tokens\":1,\"output_tokens\":2,\"cache_tokens\":3}]}";

    private static final String SOLUTIONS = "{\"session_id\":\"SID-S\",\"solutions\":{\"proposed\":[\"a\",\"b\"],\"chosen\":\"c\"}}";

    private static final String FINISHED_ONLY = "{\"session_id\":\"SID-D\",\"finished_at\":\"2026-11-10T10:00:00\"}";

    @Test
    void parsesActionsShape() {
        IngestJsonParser.ParseOutcome outcome = parser.parseContent(ACTIONS, "src");

        assertTrue(outcome.isSuccessful());
        IngestedSession session = outcome.getSession();
        assertEquals("SID-1", session.getSessionId());
        assertEquals("TASK-1", session.getTaskId());
        assertEquals("Task 1", session.getName());
        assertEquals(2, session.getSteps().size());
        assertEquals("first", session.getSteps().get(0).getText());
        assertEquals("second", session.getSteps().get(1).getText());
        assertTrue(session.anyUsage().isEmpty());
    }

    @Test
    void parsesOpencodeMessagesShape() {
        IngestJsonParser.ParseOutcome outcome = parser.parseContent(MESSAGES, "src");

        assertTrue(outcome.isSuccessful());
        IngestedSession session = outcome.getSession();
        assertEquals(2, session.getSteps().size());
        assertEquals("M-1", session.getSteps().get(0).getMessageId());
        assertEquals("M-2", session.getSteps().get(1).getMessageId());
    }

    @Test
    void bothShapesProduceEquivalentSteps() {
        IngestedSession fixture = parser.parseContent(ACTIONS_EQ, "src").getSession();
        IngestedSession encoded = parser.parseContent(MESSAGES_EQ, "src").getSession();

        assertEquals(fixture.getSteps().size(), encoded.getSteps().size());
        for (int i = 0; i < fixture.getSteps().size(); i++) {
            assertEquals(fixture.getSteps().get(i).getText(),
                    encoded.getSteps().get(i).getText());
        }
    }

    @Test
    void caseInsensitiveFieldNames() {
        IngestJsonParser.ParseOutcome outcome = parser.parseContent(CI, "src");

        assertTrue(outcome.isSuccessful(), "single object must parse");
        IngestedSession session = outcome.getSession();
        // Case-insensitive: SESSION_ID -> session_id; TITle -> title.
        assertEquals("SID-CI", session.getSessionId());
        assertEquals("Title Title", session.getName());
    }

    @Test
    void unknownFieldsIgnored() {
        IngestJsonParser.ParseOutcome outcome = parser.parseContent(UNKNOWN, "src");

        assertTrue(outcome.isSuccessful());
        IngestedSession session = outcome.getSession();
        assertEquals("SID-UF", session.getSessionId());
        assertEquals("Keep Me", session.getName());
        assertEquals(0, session.getSteps().size());
    }

    @Test
    void missingSessionIdFails() {
        IngestJsonParser.ParseOutcome outcome = parser.parseContent(MISSING_SID, "src");

        assertFalse(outcome.isSuccessful());
        assertNotNull(outcome.getError());
    }

    @Test
    void invalidJsonFails() {
        IngestJsonParser.ParseOutcome outcome = parser.parseContent("{ not valid json", "src");

        assertFalse(outcome.isSuccessful());
        assertNotNull(outcome.getError());
    }

    @Test
    void nullUsageProducesNoRows() {
        IngestedSession withNull = parser.parseContent(NULL_USAGE, "src").getSession();
        IngestedSession without = parser.parseContent(NO_USAGE, "src").getSession();

        assertTrue(withNull.getUsages().isEmpty());
        assertTrue(without.getUsages().isEmpty());
    }

    @Test
    void usageAlternateKeys() {
        IngestedSession session = parser.parseContent(USAGE_KEYS, "src").getSession();

        assertEquals(2, session.getUsages().size());
        IngestedSession.IngestedUsage shortKeys = session.getUsages().get(0);
        assertEquals("m1", shortKeys.getModel());
        assertEquals(10L, shortKeys.getInputTokens());
        assertEquals(20L, shortKeys.getOutputTokens());
        assertEquals(5L, shortKeys.getCacheTokens());
        IngestedSession.IngestedUsage longKeys = session.getUsages().get(1);
        assertEquals("m2", longKeys.getModel());
        assertEquals(1L, longKeys.getInputTokens());
        assertEquals(2L, longKeys.getOutputTokens());
        assertEquals(3L, longKeys.getCacheTokens());
    }

    @Test
    void solutionsProposedAndChosen() {
        IngestedSession session = parser.parseContent(SOLUTIONS, "src").getSession();

        List<IngestedSession.IngestedSolution> solutions = session.getSolutions();
        assertEquals(3, solutions.size());
        assertEquals("proposed", solutions.get(0).getKind());
        assertEquals("proposed", solutions.get(1).getKind());
        assertEquals("chosen", solutions.get(2).getKind());
        assertEquals("a", solutions.get(0).getText());
        assertEquals("c", solutions.get(2).getText());
        assertNull(solutions.get(2).getSourceMessageId());
    }

    @Test
    void deriveStatusCompletedWhenFinishedAtSet() {
        IngestedSession session = parser.parseContent(FINISHED_ONLY, "src").getSession();

        assertEquals(TaskStatus.COMPLETED, parser.deriveStatus(session));
    }
}
