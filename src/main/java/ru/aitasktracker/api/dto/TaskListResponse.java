package ru.aitasktracker.api.dto;

import java.util.List;

/**
 * The task list payload (contracts §4.2): a grand total and a single page of
 * items. Empty state is {@code { total: 0, items: [] }} at 200 (contracts §1).
 */
public class TaskListResponse {

    private final int total;
    private final List<TaskListItem> items;

    public TaskListResponse(int total, List<TaskListItem> items) {
        this.total = total;
        this.items = items;
    }

    public int getTotal() {
        return total;
    }

    public List<TaskListItem> getItems() {
        return items;
    }
}
