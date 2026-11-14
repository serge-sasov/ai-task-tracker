package ru.aitasktracker.ingestion.domain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ru.aitasktracker.domain.TaskStatus;
import ru.aitasktracker.domain.TaskStatusService;

/**
 * Defensive JSON parser for opencode session files (contracts §3).
 *
 * <p>Accepts BOTH input shapes and maps them to a common
 * {@link IngestedSession}:
 * <ul>
 *   <li>fixture — {@code actions:[{at,text}]}, structured
 *       {@code solutions:{proposed:[string],chosen:string}};</li>
 *   <li>opencode — {@code messages:[{message_id,at,text}]}, same structured
 *       solutions.</li>
 * </ul>
 *
 * <p>Field names are matched case-insensitively; unknown fields are ignored. One
 * bad file never throws out of {@link #parseFile(Path)} — instead the error is
 * captured in a {@link ParseOutcome} so callers can record it in
 * {@code ingest_state.parse_errors} and keep ingesting the other files (ADR-0001
 * risks, contracts §3).
 */
public class IngestJsonParser {

    private static final DateTimeFormatter[] FORMATS = {
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    };

    private final TaskStatusService statusService = new TaskStatusService();

    private final ObjectMapper mapper = new ObjectMapper();

    /** Outcome of a single-file parse. Never thrown out of {@link #parseFile}. */
    public static final class ParseOutcome {

        private final IngestedSession session;
        private final String error;

        private ParseOutcome(IngestedSession session, String error) {
            this.session = session;
            this.error = error;
        }

        public static ParseOutcome success(IngestedSession session) {
            return new ParseOutcome(session, null);
        }

        public static ParseOutcome failure(String error) {
            return new ParseOutcome(null, error);
        }

        public boolean isSuccessful() {
            return session != null;
        }

        public IngestedSession getSession() {
            return session;
        }

        public String getError() {
            return error;
        }
    }

    /**
     * Parse one JSON file into an {@link IngestedSession}. Never throws on bad
     * JSON / unexpected content — returns a {@link ParseOutcome#failure(String)}.
     *
     * @param file       path to a JSON session document
     * @param sourcePath logical source path recorded on the task (contracts §2)
     * @return the parse outcome, successful or captured-error
     */
    public ParseOutcome parseFile(Path file, String sourcePath) {
        String raw;
        try {
            raw = Files.readString(file);
        } catch (IOException e) {
            return ParseOutcome.failure("read error: " + e.getMessage());
        }
        return parseContent(raw, sourcePath);
    }

    /**
     * Parse a JSON document string. Used by both {@link #parseFile(Path, String)}
     * and unit tests.
     */
    public ParseOutcome parseContent(String raw, String sourcePath) {
        JsonNode root;
        try {
            root = mapper.readTree(raw);
        } catch (IOException e) {
            return ParseOutcome.failure("invalid JSON: " + e.getMessage());
        }
        return parseNode(root, sourcePath);
    }

    private ParseOutcome parseNode(JsonNode root, String sourcePath) {
        if (root == null || !root.isObject()) {
            return ParseOutcome.failure("root JSON value is not an object");
        }
        IngestedSession session = new IngestedSession();
        session.setSourcePath(sourcePath);

        String sessionId = firstNonNull(
                text(root, "session_id"), text(root, "sessionID"));
        if (sessionId == null || sessionId.isBlank()) {
            return ParseOutcome.failure("required field 'session_id' missing or empty");
        }
        session.setSessionId(sessionId.trim());

        String taskId = text(root, "task_id");
        if (taskId == null || taskId.isBlank()) {
            taskId = deriveTaskId(sessionId);
        }
        session.setTaskId(taskId.trim());

        session.setName(nullToEmpty(text(root, "title")).trim());

        String startedAt = firstNonNull(text(root, "started_at"), text(root, "startAt"));
        session.setStartedAt(parseDateTime(startedAt));

        String finishedAt = firstNonNull(text(root, "finished_at"), text(root, "finishedAt"));
        session.setFinishedAt(parseDateTime(finishedAt));

        session.setRawStatus(nullToEmpty(text(root, "final_status")).trim());

        parseSteps(root, session);
        parseSolutions(root, session);
        parseUsage(root, session);

        return ParseOutcome.success(session);
    }

    /**
     * Both shapes carry the steps. The {@code messages} opencode shape supplies a
     * {@code message_id}; the {@code actions} fixture shape omits it — derive a
     * stable message id from the position so the row is keyed and deduplicated.
     */
    private void parseSteps(JsonNode root, IngestedSession session) {
        List<JsonNode> steps = resolveSteps(root);
        for (int i = 0; i < steps.size(); i++) {
            JsonNode step = steps.get(i);
            String messageId = firstNonNull(text(step, "message_id"), text(step, "messageId"));
            if (messageId == null || messageId.isBlank()) {
                messageId = "action-" + (i + 1);
            }

            LocalDateTime at = null;
            String atRaw = firstNonNull(text(step, "at"), text(step, "time"));
            at = parseDateTime(atRaw);

            String sText = firstNonNull(text(step, "text"), text(step, "action"));
            session.addStep(
                    new IngestedSession.IngestedStep(messageId.trim(), at, sText));
        }
    }

    private List<JsonNode> resolveSteps(JsonNode root) {
        JsonNode messages = root.get("messages");
        if (messages != null && messages.isArray()) {
            return arrayToNodes(messages);
        }
        JsonNode actions = root.get("actions");
        if (actions != null && actions.isArray()) {
            return arrayToNodes(actions);
        }
        return Collections.emptyList();
    }

    private static List<JsonNode> arrayToNodes(JsonNode array) {
        List<JsonNode> out = new ArrayList<>();
        for (JsonNode n : array) {
            out.add(n);
        }
        return out;
    }

    private void parseSolutions(JsonNode root, IngestedSession session) {
        JsonNode solutions = root.get("solutions");
        if (solutions == null || !solutions.isObject()) {
            return;
        }
        JsonNode proposed = solutions.get("proposed");
        if (proposed != null && proposed.isArray()) {
            proposed.forEach(node -> {
                if (node.isTextual()) {
                    session.addSolution(
                            new IngestedSession.IngestedSolution("proposed",
                                    node.asText().trim(), null));
                }
            });
        }
        JsonNode chosen = solutions.get("chosen");
        if (chosen != null && chosen.isTextual()) {
            session.addSolution(
                    new IngestedSession.IngestedSolution("chosen",
                            chosen.asText().trim(), null));
        }
    }

    private void parseUsage(JsonNode root, IngestedSession session) {
        JsonNode usage = root.get("usage");
        if (usage == null || !usage.isArray() || usage.isEmpty()) {
            return;
        }
        for (JsonNode entry : usage) {
            if (entry == null || !entry.isObject()) {
                continue;
            }
            String model = text(entry, "model");
            if (model == null || model.isBlank()) {
                continue;
            }
            long input = firstLong(entry, "input_tokens", "input");
            long output = firstLong(entry, "output_tokens", "output");
            long cache = firstLong(entry, "cache_tokens", "cache");
            session.addUsage(
                    new IngestedSession.IngestedUsage(model.trim(), input, output, cache));
        }
    }

    /** Case-insensitive field lookup (contracts §3). Unknown fields are ignored. */
    private String text(JsonNode node, String field) {
        if (node == null || !node.isObject()) {
            return null;
        }
        String lowered = field.toLowerCase(Locale.ROOT);
        java.util.Iterator<java.util.Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            java.util.Map.Entry<String, JsonNode> entry = fields.next();
            if (entry.getKey().toLowerCase(Locale.ROOT).equals(lowered)) {
                JsonNode value = entry.getValue();
                if (value.isMissingNode() || value.isNull()) {
                    return null;
                }
                return value.asText();
            }
        }
        return null;
    }

    private static String firstNonNull(String a, String b) {
        if (a != null && !a.isEmpty()) {
            return a;
        }
        return b;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static long firstLong(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode value = node.get(key);
            if (value != null && !value.isNull()
                    && (value.isNumber() || value.isTextual())) {
                try {
                    return value.asLong();
                } catch (NumberFormatException ignored) {
                    // fall through to next key
                }
            }
        }
        return 0L;
    }

    private static String deriveTaskId(String sessionId) {
        return "TASK-" + Math.abs(sessionId.hashCode()) % 100000;
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        for (DateTimeFormatter formatter : FORMATS) {
            try {
                return LocalDateTime.parse(trimmed, formatter);
            } catch (DateTimeParseException ignored) {
                // try next format
            }
        }
        return null;
    }

    /**
     * Derive the canonical {@link TaskStatus} for this session (contracts §3 /
     * §2): the source {@code final_status} when canonical, else derived from
     * {@code finished_at}.
     */
    public TaskStatus deriveStatus(IngestedSession session) {
        return statusService.deriveStatus(session.getRawStatus(), session.getFinishedAt());
    }
}
