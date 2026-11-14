package ru.aitasktracker.domain;

import java.util.Arrays;
import java.util.Optional;

/**
 * Canonical task status (RU, contracts §1 / §2, data-model note).
 *
 * <p>The string form is stored verbatim in {@code tasks.status} and is matched
 * against the SQL CHECK constraint {@code IN ('Завершена','Прервана','Не завершена')}.
 */
public enum TaskStatus {

    COMPLETED("Завершена"),
    INTERRUPTED("Прервана"),
    NOT_COMPLETED("Не завершена");

    private final String value;

    TaskStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    /** Case-insensitive lookup by the canonical RU string; empty if unmatched. */
    public static Optional<TaskStatus> fromValue(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String normalized = raw.trim();
        if (normalized.isEmpty()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(s -> s.value.equalsIgnoreCase(normalized))
                .findFirst();
    }
}
