package ru.aitasktracker.api.calendar.dto;

/**
 * One month cell of the {@code year} view (contracts §4.5, API-002 MonthCell).
 * {@code null} values render «Нет данных» on the calendar; months with data
 * carry their counts so clicking switches the SPA to the month view (contracts
 * §4.5, Q-UX-004).
 */
public class MonthCell {

    private final String date;
    private final Integer taskCount;
    private final Integer totalDurationS;
    private final Integer tokensTotal;

    public MonthCell(String date, Integer taskCount, Integer totalDurationS, Integer tokensTotal) {
        this.date = date;
        this.taskCount = taskCount;
        this.totalDurationS = totalDurationS;
        this.tokensTotal = tokensTotal;
    }

    public String getDate() {
        return date;
    }

    public Integer getTaskCount() {
        return taskCount;
    }

    public Integer getTotalDurationS() {
        return totalDurationS;
    }

    public Integer getTotalTokens() {
        return tokensTotal;
    }
}
