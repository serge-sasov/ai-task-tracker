package ru.aitasktracker.domain;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Status derivation and consistency invariants from contracts §2:
 *
 * <ul>
 *   <li>{@code finished_at} NULL &Rightarrow; status = {@code 'Не завершена'}</li>
 *   <li>status = {@code 'Завершена'} &Rightarrow; {@code finished_at} NOT NULL</li>
 * </ul>
 *
 * The status for a session is taken from the source {@code final_status} when it
 * names a canonical value; otherwise it is derived from {@code finishedAt}
 * (session end state). A session with no end state is {@link TaskStatus#NOT_COMPLETED}.
 */
public class TaskStatusService {

    /**
     * Derive the task status.
     *
     * @param sourceFinalStatus raw RU string from the source session (may be blank); preferred
     * @param finishedAt        session end time (nullable)
     * @return derived or explicitly provided canonical status
     */
    public TaskStatus deriveStatus(String sourceFinalStatus, LocalDateTime finishedAt) {
        Optional<TaskStatus> fromSource = TaskStatus.fromValue(sourceFinalStatus);
        if (fromSource.isPresent()) {
            return fromSource.get();
        }
        return derivedFromEndState(finishedAt);
    }

    private TaskStatus derivedFromEndState(LocalDateTime finishedAt) {
        if (finishedAt != null) {
            return TaskStatus.COMPLETED;
        }
        return TaskStatus.NOT_COMPLETED;
    }

    /**
     * Pure invariant check (contracts §2):
     * finishedAt NULL &Rightarrow; status must not be COMPLETED;
     * status COMPLETED &Rightarrow; finishedAt must not be null.
     *
     * @return {@code true} when status and finishedAt are mutually consistent
     */
    public boolean isConsistent(TaskStatus status, LocalDateTime finishedAt) {
        if (status == TaskStatus.COMPLETED && finishedAt == null) {
            return false;
        }
        if (finishedAt == null && status == TaskStatus.NOT_COMPLETED) {
            return true;
        }
        return true;
    }

    /**
     * Enforce the invariant, throwing when status and finishedAt contradict.
     *
     * @throws IllegalArgumentException when {@code finished_at NULL ⇒ status='Не завершена'}
     *                                  or {@code status='Завершена' ⇒ finished_at NOT NULL} is violated
     */
    public void validateConsistency(TaskStatus status, LocalDateTime finishedAt) {
        if (!isConsistent(status, finishedAt)) {
            throw new IllegalArgumentException(
                    "Inconsistent task state: finished_at=" + finishedAt + " status=" + status.value());
        }
    }
}
