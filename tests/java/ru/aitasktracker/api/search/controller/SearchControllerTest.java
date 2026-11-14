package ru.aitasktracker.api.search.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ru.aitasktracker.api.dto.SearchRequest;
import ru.aitasktracker.api.dto.SearchResponse;
import ru.aitasktracker.api.search.exception.EmptyQueryException;
import ru.aitasktracker.api.search.exception.SearchUnavailableException;
import ru.aitasktracker.api.search.service.SearchService;

/**
 * Plain unit test for {@link SearchController}. The controller delegates to a
 * mocked {@link SearchService}; the exception-to-HTTP mapping is the shared
 * {@code ApiErrorAdapter}'s job (tested in the context), so here we assert the
 * controller forwards arguments unchanged and throws when the service does.
 */
class SearchControllerTest {

    private SearchService searchService;
    private SearchController controller;

    @BeforeEach
    void setUp() {
        searchService = mock(SearchService.class);
        controller = new SearchController(searchService);
    }

    @Test
    void delegatesBodyToService() {
        SearchResponse resp = new SearchResponse("ci", 0, java.util.List.of());
        when(searchService.search("ci", 5, "TASK-001")).thenReturn(resp);

        SearchResponse out = controller.search(new SearchRequest("ci", 5, "TASK-001"));

        assertEquals(0, out.getTotal());
        verify(searchService).search("ci", 5, "TASK-001");
    }

    @Test
    void emptyQueryPropagatesToCaller() {
        when(searchService.search(any(), any(), any()))
                .thenThrow(new EmptyQueryException());

        assertThrows(EmptyQueryException.class,
                () -> controller.search(new SearchRequest("", 5, null)));
    }

    @Test
    void ollamaFailurePropagatesToCaller() {
        when(searchService.search(any(), any(), any()))
                .thenThrow(new SearchUnavailableException("x"));

        assertThrows(SearchUnavailableException.class,
                () -> controller.search(new SearchRequest("ci", 5, null)));
    }
}
