package ru.aitasktracker.api.calendar.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ru.aitasktracker.api.calendar.dto.CalendarResponse;
import ru.aitasktracker.api.calendar.service.CalendarService;

/**
 * Calendar endpoint {@code GET /api/calendar} (contracts §4.5, API-002). The
 * {@code view} defaults to {@code week}; {@code date} is an anchor day
 * ({@code YYYY-MM-DD}). Bad/absent date or unknown view yields 400
 * {@code INVALID_PERIOD} «Некорректная дату» via the shared envelope handler.
 */
@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping
    public CalendarResponse calendar(
            @RequestParam(value = "view", defaultValue = "week") String view,
            @RequestParam(value = "date", required = false) String date) {
        return calendarService.calendar(view, date);
    }
}
