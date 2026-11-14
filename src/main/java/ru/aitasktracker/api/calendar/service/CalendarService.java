package ru.aitasktracker.api.calendar.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import ru.aitasktracker.api.calendar.dto.CalendarDay;
import ru.aitasktracker.api.calendar.dto.CalendarResponse;
import ru.aitasktracker.api.calendar.dto.CalendarTask;
import ru.aitasktracker.api.calendar.dto.MonthCell;
import ru.aitasktracker.api.exception.InvalidPeriodException;
import ru.aitasktracker.domain.TaskDuration;
import ru.aitasktracker.domain.entity.TaskEntity;
import ru.aitasktracker.domain.entity.TokenUsageEntity;
import ru.aitasktracker.domain.repository.TaskRepository;
import ru.aitasktracker.domain.repository.TokenUsageRepository;

/**
 * Calendar query service (contracts §4.5, API-002). Pure orchestration over the
 * task/token repositories: builds the {@code day}/{@code week}/{@code month}/
 * {@code year} grids for the anchor date. Every repository call is an existing
 * method, so this class needs no new repository surface.
 *
 * <p>{@code view} is one of {@code day|week|month|year} (the {@code week}
 * default is enforced by the controller). A {@code date} is an anchor day
 * ({@code YYYY-MM-DD}); an absent or unparseable value throws
 * {@code InvalidPeriodException} «Некорректная дату» (contracts §4.5). An empty
 * period still returns the full grid with empty task cells (contracts §4.5,
 * R-008); the {@code year} grid always carries twelve cells whose null values
 * render «Нет данных».
 *
 * <p>The week and month grids are Mon–Sun; days outside the anchor week or the
 * anchor month carry {@code outside = true} so the SPA dims them (contracts §4.5,
 * Q-UX-004).
 */
@Service
public class CalendarService {

    private static final String[] VIEW_ENUM = {"day", "week", "month", "year"};
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final int MAX_ROWS = 100000;

    private final TaskRepository taskRepository;
    private final TokenUsageRepository tokenUsageRepository;

    public CalendarService(TaskRepository taskRepository,
                           TokenUsageRepository tokenUsageRepository) {
        this.taskRepository = taskRepository;
        this.tokenUsageRepository = tokenUsageRepository;
    }

    public CalendarResponse calendar(String view, String date) {
        String v = normalizeView(view);
        LocalDate anchor = parseAnchor(date);
        switch (v) {
            case "day" -> {
                List<CalendarTask> tasks = tasksOnDay(anchor);
                List<CalendarDay> days = List.of(new CalendarDay(
                        anchor.toString(), false, tasks));
                return new CalendarResponse(v, anchor.toString(), days, List.of());
            }
            case "week" -> {
                LocalDate monday = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                return new CalendarResponse(v, anchor.toString(), weekDays(monday), List.of());
            }
            case "month" -> {
                YearMonth ym = YearMonth.from(anchor);
                LocalDate monday = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                return new CalendarResponse(v, anchor.toString(), monthDays(ym, monday), List.of());
            }
            default -> {
                List<MonthCell> months = yearCells(anchor.getYear());
                return new CalendarResponse(v, anchor.toString(), List.of(), months);
            }
        }
    }

    private String normalizeView(String view) {
        if (view == null || view.isBlank()) {
            return "week";
        }
        String v = view.trim().toLowerCase(Locale.ROOT);
        for (String allowed : VIEW_ENUM) {
            if (allowed.equals(v)) {
                return v;
            }
        }
        throw new InvalidPeriodException("Некорректная дату");
    }

    private LocalDate parseAnchor(String date) {
        if (date == null || date.isBlank()) {
            throw new InvalidPeriodException("Некорректная дату");
        }
        try {
            return LocalDate.parse(date.trim());
        } catch (RuntimeException e) {
            throw new InvalidPeriodException("Некорректная дату");
        }
    }

    private List<CalendarDay> weekDays(LocalDate monday) {
        List<CalendarDay> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            days.add(new CalendarDay(monday.plusDays(i).toString(),
                    false, tasksOnDay(monday.plusDays(i))));
        }
        return days;
    }

    private List<CalendarDay> monthDays(YearMonth ym, LocalDate gridMonday) {
        List<CalendarDay> days = new ArrayList<>();
        LocalDate cursor = gridMonday;
        while (!cursor.isAfter(ym.atEndOfMonth().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)))) {
            boolean inMonth = !cursor.isBefore(ym.atDay(1)) && !cursor.isAfter(ym.atEndOfMonth());
            days.add(new CalendarDay(cursor.toString(), !inMonth, tasksOnDay(cursor)));
            cursor = cursor.plusDays(1);
        }
        return days;
    }

    private List<MonthCell> yearCells(int year) {
        List<MonthCell> cells = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            YearMonth ym = YearMonth.of(year, m);
            List<TaskEntity> tasks = windowTasks(ym.atDay(1).atStartOfDay(),
                    ym.plusMonths(1).atDay(1).atStartOfDay());
            if (tasks.isEmpty()) {
                cells.add(new MonthCell(ym.atDay(1).toString(), null, null, null));
            } else {
                cells.add(new MonthCell(ym.atDay(1).toString(), tasks.size(),
                        (int) durationSeconds(tasks), (int) tokenTokens(tasks)));
            }
        }
        return cells;
    }

    private List<CalendarTask> tasksOnDay(LocalDate day) {
        Page<TaskEntity> page = taskRepository.findByStartedAtBetween(
                day.atStartOfDay(), day.plusDays(1).atStartOfDay(),
                PageRequest.of(0, MAX_ROWS));
        List<CalendarTask> cells = new ArrayList<>();
        for (TaskEntity task : page.getContent()) {
            cells.add(new CalendarTask(task.getTaskId(),
                    task.getStartedAt().format(TS_FMT),
                    task.getFinishedAt() == null ? null
                            : task.getFinishedAt().format(TS_FMT),
                    durationSecondsOf(task)));
        }
        return cells;
    }

    private List<TaskEntity> windowTasks(LocalDateTime from, LocalDateTime to) {
        return taskRepository.findByStartedAtBetween(from, to, PageRequest.of(0, MAX_ROWS)).getContent();
    }

    private long durationSeconds(List<TaskEntity> tasks) {
        long total = 0L;
        for (TaskEntity task : tasks) {
            total += durationSecondsOf(task);
        }
        return total;
    }

    private long durationSecondsOf(TaskEntity task) {
        if (task.getFinishedAt() == null) {
            return 0L;
        }
        return TaskDuration.seconds(task.getStartedAt(), task.getFinishedAt(), null);
    }

    private long tokenTokens(List<TaskEntity> tasks) {
        long total = 0L;
        for (TaskEntity task : tasks) {
            for (TokenUsageEntity row : tokenUsageRepository.findByTaskId(task.getTaskId())) {
                total += row.getInputTokens() + row.getOutputTokens() + row.getCacheTokens();
            }
        }
        return total;
    }
}
