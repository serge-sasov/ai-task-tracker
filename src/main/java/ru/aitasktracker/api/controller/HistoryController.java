package ru.aitasktracker.api.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.aitasktracker.api.exception.ConfirmationRequiredException;
import ru.aitasktracker.api.service.HistoryService;

/**
 * History deletion endpoint (contracts section 4.7, API-001). The body
 * {@code { "confirm": true }} is mandatory: no body, missing {@code confirm}, or a
 * value other than {@code true} yields 400 {@code CONFIRMATION_REQUIRED}. On
 * success returns 200 {@code { "deleted": true }}; an empty data set returns 200
 * {@code { "deleted": false }} (no-op, not an error).
 */
@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @DeleteMapping
    public HistoryResult delete(@RequestBody(required = false) ConfirmBody body) {
        boolean confirmed = body != null && Boolean.TRUE.equals(body.getConfirm());
        if (!confirmed) {
            throw new ConfirmationRequiredException(
                    ru.aitasktracker.api.exception.ConfirmationRequiredException.MESSAGE);
        }
        boolean deleted = historyService.deleteAll();
        return new HistoryResult(deleted);
    }

    /**
     * Request body for {@code DELETE /api/history}. {@code confirm} must be
     * present and {@code true}.
     */
    public static class ConfirmBody {
        private Boolean confirm;

        public Boolean getConfirm() {
            return confirm;
        }

        public void setConfirm(Boolean confirm) {
            this.confirm = confirm;
        }
    }

    public static class HistoryResult {
        private final boolean deleted;

        public HistoryResult(boolean deleted) {
            this.deleted = deleted;
        }

        public boolean isDeleted() {
            return deleted;
        }
    }
}
