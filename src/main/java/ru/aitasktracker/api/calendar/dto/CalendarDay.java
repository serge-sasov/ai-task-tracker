package ru.aitasktracker.api.calendar.dto;

import java.util.List;

/**
 * One day cell of the calendar grid (contracts §4.5, API-002 CalendarDay). When
 * the view is {@code week} or {@code month} adjacent days outside the anchor
 * period are carried with {@code outside = true} so the SPA dims them and keeps
 * the Mon–Sun ordering stable (contracts §4.5, Q-UX-004).
 */
public class CalendarDay {

    private final String date;
    private final boolean outside;
    private final List<CalendarTask> tasks;

    public CalendarDay(String date, boolean outside, List<CalendarTask> tasks) {
        this.date = date;
        this.outside = outside;
        this.tasks = tasks;
    }

    public String getDate() {
        return date;
    }

    public boolean isOutside() {
        return outside;
    }

    public List<CalendarTask> getTasks() {
        return tasks;
    }
}
