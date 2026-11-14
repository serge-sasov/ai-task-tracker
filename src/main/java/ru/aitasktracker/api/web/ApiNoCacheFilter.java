package ru.aitasktracker.api.web;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Adds {@code Cache-Control: no-store} to every {@code GET} response under
 * {@code /api/*} (contracts section 1 / caching rule: no-store on all GET /api/*).
 * Non-API or non-GET requests are passed through untouched.
 */
@Component
public class ApiNoCacheFilter extends OncePerRequestFilter {

    private static final String CACHE_NO_STORE = "no-store";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        if ("GET".equalsIgnoreCase(request.getMethod())
                && request.getRequestURI().startsWith("/api/")) {
            response.setHeader("Cache-Control", CACHE_NO_STORE);
        }
        filterChain.doFilter(request, response);
    }
}
