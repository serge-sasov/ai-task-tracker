package ru.aitasktracker.domain;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates that Flyway migration files exist and carry the expected schema
 * elements, without opening a database connection. This lets the migration set be
 * verified inside `mvn test` (no Postgres required).
 */
class FlywayMigrationsTest {

    private final Path migrationRoot = resolveMigrationRoot();

    private Path resolveMigrationRoot() {
        // Tests run with CWD = backend/. Prefer the canonical repo-root migrations;
        // fall back to the build layout, then to the old backend/src path.
        String[] candidates = {
                "../src/main/resources/db/migration",
                "src/main/resources/db/migration",
                "backend/src/main/resources/db/migration"
        };
        for (String c : candidates) {
            Path candidate = Paths.get(c);
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
        }
        // Last resort: return the repo-root path so a missing directory produces a
        // clear assertion failure rather than a silent skip.
        return Paths.get("../src/main/resources/db/migration");
    }

    private List<String> readMigrationFiles() {
        try (Stream<Path> stream = Files.walk(migrationRoot)) {
            return stream.filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Migration directory not found: " + migrationRoot, e);
        }
    }

    @Test
    void migrationsDirectoryExists() {
        assertTrue(Files.isDirectory(migrationRoot),
                "Migration directory missing: " + migrationRoot);
    }

    @Test
    void v1VectorExtensionFirst() {
        List<String> names = readMigrationFiles();
        assertTrue(names.size() >= 1, "at least one migration required");
        assertTrue(names.get(0).startsWith("V1"),
                "first migration must be V1, got: " + names.get(0));
        String v1 = readMigration(1);
        assertTrue(v1.contains("CREATE EXTENSION IF NOT EXISTS vector"),
                "V1 must enable the pgvector extension: " + v1);
    }

    @Test
    void v2SchemaCreatesAllTables() {
        String v2 = readMigration(2);
        assertTrue(v2.contains("CREATE TABLE tasks"), "missing tasks table");
        assertTrue(v2.contains("CREATE TABLE steps"), "missing steps table");
        assertTrue(v2.contains("CREATE TABLE solutions"), "missing solutions table");
        assertTrue(v2.contains("CREATE TABLE token_usage"), "missing token_usage table");
        assertTrue(v2.contains("CREATE TABLE ingest_state"), "missing ingest_state table");
    }

    @Test
    void tasksTableHasVectorColumn() {
        String v2 = readMigration(2);
        assertTrue(v2.contains("vector(1024)"),
                "tasks.embedding must be a vector(1024) column");
    }

    @Test
    void statusCheckConstraintPresent() {
        String v2 = readMigration(2);
        assertTrue(v2.contains("CHECK (status IN"), "tasks.status CHECK constraint missing");
        assertTrue(v2.contains("Завершена"), "status CHECK must include the RU values");
        assertTrue(v2.contains("Прервана"));
        assertTrue(v2.contains("Не завершена"));
    }

    @Test
    void foreignKeysCascadeDelete() {
        String v2 = readMigration(2);
        Pattern fk = Pattern.compile("REFERENCES tasks \\(task_id\\) ON DELETE CASCADE");
        // steps, solutions, token_usage all cascade-delete; count matches.
        int count = 0;
        java.util.regex.Matcher m = fk.matcher(v2);
        while (m.find()) {
            count++;
        }
        assertEquals(3, count, "exactly three FKs from steps/solutions/token_usage to tasks");
    }

    @Test
    void uniqueConstraintsPresent() {
        String v2 = readMigration(2);
        assertTrue(v2.contains("UNIQUE (task_id, message_id)"), "steps unique key missing");
        assertTrue(v2.contains("UNIQUE (task_id, kind, text, source_message_id)"),
                "solutions unique key missing");
        assertTrue(v2.contains("UNIQUE (task_id, message_id, model, scope)"),
                "token_usage unique key missing");
    }

    @Test
    void indexesDefined() {
        String v3 = readMigration(3);
        assertTrue(v3.contains("CREATE INDEX idx_steps_task"), "steps index missing");
        assertTrue(v3.contains("CREATE INDEX idx_steps_task_message"), "steps composite index missing");
        assertTrue(v3.contains("CREATE INDEX idx_solutions_task"), "solutions index missing");
        assertTrue(v3.contains("CREATE INDEX idx_token_usage_task"), "token_usage index missing");
        assertTrue(v3.contains("CREATE INDEX idx_tasks_started_at"), "tasks start index missing");
    }

    @Test
    void migrationFilesFollowVersionPattern() {
        List<String> names = readMigrationFiles();
        Pattern pattern = Pattern.compile("V\\d+__.*\\.sql");
        for (String name : names) {
            assertTrue(pattern.matcher(name).matches(),
                    "migration filename must match V<nn>__<name>.sql: " + name);
        }
    }

    private String readMigration(int version) {
        Pattern pattern = Pattern.compile("V" + version + "__.*\\.sql");
        List<String> names = readMigrationFiles();
        Optional<String> match = names.stream()
                .filter(name -> pattern.matcher(name).matches())
                .findAny();
        assertTrue(match.isPresent(), "migration V" + version + " not found");
        try {
            return Files.readString(migrationRoot.resolve(match.get()));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + match.get(), e);
        }
    }
}
