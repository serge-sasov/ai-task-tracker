package ru.aitasktracker.ingestion.domain;

/**
 * Per-cycle source status (contracts §3). Exposed via {@code /api/health} and
 * the S-01 banner (R-028).
 *
 * <ul>
 *   <li>{@link #AVAILABLE} — source files read this cycle;</li>
 *   <li>{@link #UNAVAILABLE} — source directory missing or unreadable;</li>
 *   <li>{@link #STALE} — read, but no file change for more than 3× the poll
 *       interval (R-028, contracts §3).</li>
 * </ul>
 */
public enum SourceStatus {
    AVAILABLE,
    UNAVAILABLE,
    STALE
}
