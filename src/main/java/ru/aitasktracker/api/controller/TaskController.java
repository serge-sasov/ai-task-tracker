package ru.aitasktracker.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ru.aitasktracker.api.dto.TaskDetailResponse;
import ru.aitasktracker.api.dto.TaskListResponse;
import ru.aitasktracker.api.service.TaskQueryService;

/**
 * Task list and detail endpoints (contracts section 4.2 / 4.3, API-001). Every
 * GET carries {@code Cache-Control: no-store} so the SPA refetches.
 *
 * <p>Pagination: {@code page} is 1-based (default 1), {@code size} defaults to 50
 * and is clamped to 500 (larger values are clamped, per contracts section 1).
 * {@code from}/{@code to} are ISO 8601 local wall-clock strings; unparseable values
 * yield 400 {@code INVALID_PERIOD} (contracts section 4.2). {@code status} is a RU
 * enum value, matched case-insensitively (contracts section 1).
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    public static final int DEFAULT_SIZE = 50;

    private final TaskQueryService taskQueryService;

    public TaskController(TaskQueryService taskQueryService) {
        this.taskQueryService = taskQueryService;
    }

    @GetMapping
    public TaskListResponse list(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "50") int size,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to,
            @RequestParam(value = "status", required = false) String status) {
        return taskQueryService.list(page, size, from, to, status);
    }

    @GetMapping("/{taskId}")
    public TaskDetailResponse detail(@PathVariable("taskId") String taskId) {
        return taskQueryService.detail(taskId);
    }
}
