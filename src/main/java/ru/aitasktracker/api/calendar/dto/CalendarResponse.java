package ru.aitasktracker.api.calendar.dto;

import java.util.List;

/**
 * Response body for {@code GET /api/calendar} (contracts §4.5, API-002
 * CalendarResponse). {@code days} carries the day/week/month grid cells and
 * {@code months} carries the twelve year cells; empty periods yield empty
 * collections so the SPA shows the «Нет задач» state (contracts §4.5, R-008).
 */
public class CalendarResponse {

    private final String view;
    private final String date;
    private final List<CalendarDay> days;
    private final List<MonthCell> months;

    public CalendarResponse(String view, String date, List<CalendarDay> days, List<MonthCell> months) {
        this.view = view;
        this.date = date;
        this.days = days;
        this.months = months;
    }

    public String getView() {
        return view;
    }

    public String getDate() {
        return date;
    }

    public List<CalendarDay> getDays() {
        return days;
    }

    public List<MonthCell> getMonths() {
        return months;
    }
}
