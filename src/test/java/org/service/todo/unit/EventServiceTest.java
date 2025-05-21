package org.service.todo.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.service.todo.model.Event;
import org.service.todo.model.Priority;
import org.service.todo.repository.Repository;
import org.service.todo.service.EventService;

import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private Repository repository;

    @InjectMocks
    private EventService eventService;

    @Test
    void addEvent_shouldCallRepositorySave() {
        Event event = new Event();
        event.setTitle("Test event");
        eventService.addEvent(event);
        verify(repository, times(1)).save(event);
    }

    @ParameterizedTest
    @MethodSource("priorityTestCases")
    void makeMakPriority_shouldSetCorrectPriorityBasedOnMarker(String title, Priority expectedPriority, String expectedTitle) {
        Event event = new Event();
        event.setTitle(title);
        Event result = eventService.makeMakPriority(event);
        assertEquals(expectedPriority, result.getPriority());
        assertEquals(expectedTitle, result.getTitle());
    }

    private static Stream<Arguments> priorityTestCases() {
        return Stream.of(
                Arguments.of("event !1", Priority.Critical, "event "),
                Arguments.of("event !2", Priority.High, "event "),
                Arguments.of("event !3", Priority.Medium, "event "),
                Arguments.of("event !4", Priority.Low, "event "),
                Arguments.of("event !55", Priority.Medium, "event "),
                Arguments.of("event !0", Priority.Medium, "event "),
                Arguments.of("event !5", Priority.Medium, "event ")
        );
    }

    @Test
    void makeMakPriority_shouldSetMediumPriorityWhenNoPriorityInTitle() {
        Event event = new Event();
        event.setTitle("Regular event");
        Event result = eventService.makeMakPriority(event);
        assertEquals(Priority.Medium, result.getPriority());
        assertEquals("Regular event", result.getTitle());
    }

    @Test
    void makeMakPriority_shouldNotOverrideExistingPriority() {
        Event event = new Event();
        event.setTitle("event !1");
        event.setPriority(Priority.High);
        Event result = eventService.makeMakPriority(event);
        assertEquals(Priority.High, result.getPriority());
        assertEquals("event ", result.getTitle());
    }

    @Test
    void makeMakPriority_shouldHandleMultiplePriorityMarkers() {
        Event event = new Event();
        event.setTitle("!1 event !2");
        Event result = eventService.makeMakPriority(event);
        assertEquals(Priority.Critical, result.getPriority());
        assertEquals(" event ", result.getTitle());
    }

    @ParameterizedTest
    @MethodSource("dateTestCases")
    void makeMakDate_shouldProcessDatesCorrectly(
            String initialTitle,
            LocalDate initialDeadline,
            LocalDate expectedDeadline,
            String expectedTitle) {
        Event event = new Event();
        event.setTitle(initialTitle);
        event.setDeadline(initialDeadline);
        Event result = eventService.makeMakDate(event);
        assertEquals(expectedDeadline, result.getDeadline());
        assertEquals(expectedTitle, result.getTitle());
    }

    private static Stream<Arguments> dateTestCases() {
        return Stream.of(
                Arguments.of(
                        "report !before 31-12-2023",
                        null,
                        LocalDate.of(2023, 12, 31),
                        "report "
                ),

                Arguments.of(
                        "report !before 31.12.2023",
                        null,
                        LocalDate.of(2023, 12, 31),
                        "report "
                ),

                Arguments.of(
                        "report !before 31-12-2023",
                        LocalDate.of(2024, 1, 15),
                        LocalDate.of(2024, 1, 15),
                        "report "
                ),

                Arguments.of(
                        "No date marker",
                        null,
                        null,
                        "No date marker"
                ),

                Arguments.of(
                        "Invalid date !before 99-99-9999",
                        null,
                        null,
                        "Invalid date "
                ),

                Arguments.of(
                        "date !before 29-02-2024",
                        null,
                        LocalDate.of(2024, 2, 29),
                        "date "
                ),

                Arguments.of(
                        "date !before 29.02-2024",
                        null,
                        null,
                        "date "
                ),

                Arguments.of(
                        "date !before 29-02.2024",
                        null,
                        null,
                        "date "
                ),

                Arguments.of(
                        "date !before 29x02z2024",
                        null,
                        null,
                        "date "
                ),
                Arguments.of(
                        "date !before 29!02*2024",
                        null,
                        null,
                        "date "
                )
        );
    }

    @Test
    void makeMakDate_shouldNotSetDeadlineWhenNoDateInTitle() {
        Event event = new Event();
        event.setTitle("Regular event");

        Event result = eventService.makeMakDate(event);

        assertNull(result.getDeadline());
        assertEquals("Regular event", result.getTitle());
    }
}
