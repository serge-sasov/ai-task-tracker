package ru.aitasktracker.api.tokens.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ru.aitasktracker.api.tokens.dto.TokenStatsResponse;
import ru.aitasktracker.api.tokens.service.TokenService;

/**
 * Token endpoint {@code GET /api/tokens} (contracts §4.6, API-002). The
 * {@code period} selects the window ({@code all} default, a single
 * {@code date}, or a {@code from}/{@code to} range where {@code to} must be
 * {@code >=} {@code from}). A bad range yields 400 {@code INVALID_PERIOD}
 * «Некорректный период» via the shared envelope handler.
 */
@RestController
@RequestMapping("/api/tokens")
public class TokensController {

    private final TokenService tokenService;

    public TokensController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @GetMapping
    public TokenStatsResponse tokens(
            @RequestParam(value = "period", defaultValue = "all") String period,
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to) {
        return tokenService.stats(period, date, from, to);
    }
}
