package org.service.todo.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.service.todo.mapper.EventMapperAdd;
import org.service.todo.model.*;
import org.service.todo.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.LocalDate;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ControllerRestAssuredTest {

    @LocalServerPort
    private int port;

    @Autowired
    private EventService eventService;

    @Autowired
    private EventMapperAdd eventMapperAdd;

    @BeforeAll
    static void setup() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @BeforeEach
    void init() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    @AfterEach
    void cleanup() {
        eventService.deleteAllEvents();
    }

    private EventDTOAdd createTestEventDTOAdd() {
        return new EventDTOAdd("Test Event", "Description",
                LocalDate.now().plusDays(1), Priority.Medium);
    }

    private EventDTOEdit createTestEventDTOEdit() {
        return new EventDTOEdit("Updated Event", "Updated Description",
                LocalDate.now().plusDays(2), Priority.High);
    }

    @Test
    void getEvents_shouldReturnEmptyList_whenNoEventsExist() {
        given()
                .when()
                .get("/events/get")
                .then()
                .statusCode(200)
                .body("", hasSize(0));
    }

    @Test
    void getEvents_shouldReturnCreatedEvents() {
        EventDTOAdd eventDTO = createTestEventDTOAdd();
        eventService.addEvent(eventMapperAdd.map(eventDTO));

        given()
                .when()
                .get("/events/get")
                .then()
                .statusCode(200)
                .body("", hasSize(1))
                .body("[0].title", equalTo("Test Event"))
                .body("[0].status", equalTo("Active"));
    }

    @Test
    void getEvents_shouldSortByStatus() {
        EventDTOAdd event1 = new EventDTOAdd("Event 1", "Desc 1",
                LocalDate.now().minusDays(1), Priority.Low);
        EventDTOAdd event2 = new EventDTOAdd("Event 2", "Desc 2",
                LocalDate.now().plusDays(10), Priority.High);

        eventService.addEvent(eventMapperAdd.map(event1));
        eventService.addEvent(eventMapperAdd.map(event2));

        List<Event> events = eventService.getEvents(null, null, null, null, null, null, null);

        given()
                .param("status", "Active")
                .when()
                .get("/events/get")
                .then()
                .statusCode(200)
                .body("[0].title", equalTo("Event 2"))
                .body("[0].status", equalTo("Active"));
    }

    @Test
    void createEvent_shouldAddNewEvent() {
        EventDTOAdd eventDTO = createTestEventDTOAdd();

        given()
                .contentType(ContentType.JSON)
                .body(eventDTO)
                .when()
                .post("/events/create")
                .then()
                .statusCode(200);

        List<Event> events = eventService.getEvents(null, null, null, null, null, null, null);
        assertEquals(1, events.size());
        assertEquals("Test Event", events.get(0).getTitle());
        assertEquals(Status.Active, events.get(0).getStatus());
    }

    @Test
    void createEvent_shouldValidateInput() {
        EventDTOAdd invalidEvent = new EventDTOAdd("", "Description",
                LocalDate.now().plusDays(1), Priority.Medium);

        given()
                .contentType(ContentType.JSON)
                .body(invalidEvent)
                .when()
                .post("/events/create")
                .then()
                .statusCode(400);

        EventDTOAdd pastDateEvent = new EventDTOAdd("Title", "Description",
                LocalDate.now().minusDays(1), Priority.Medium);

        given()
                .contentType(ContentType.JSON)
                .body(pastDateEvent)
                .when()
                .post("/events/create")
                .then()
                .statusCode(200);
    }

    @Test
    void editEvent_shouldUpdateExistingEvent() {
        EventDTOAdd eventDTO = createTestEventDTOAdd();
        eventService.addEvent(eventMapperAdd.map(eventDTO));
        long eventId = eventService.getEvents(null, null, null, null, null, null, null).get(0).getId();

        EventDTOEdit updatedEvent = createTestEventDTOEdit();

        given()
                .contentType(ContentType.JSON)
                .body(updatedEvent)
                .when()
                .put("/events/edit/" + eventId)
                .then()
                .statusCode(200);

        Event editedEvent = eventService.getById(eventId);
        assertEquals("Updated Event", editedEvent.getTitle());
        assertEquals(Status.Active, editedEvent.getStatus());
    }

    @Test
    void editEvent_shouldReturnNotFoundForNonExistentId() {
        EventDTOEdit updatedEvent = createTestEventDTOEdit();

        given()
                .contentType(ContentType.JSON)
                .body(updatedEvent)
                .when()
                .put("/events/edit/999")
                .then()
                .statusCode(404);
    }

    @Test
    void deleteEvent_shouldRemoveEvent() {
        EventDTOAdd eventDTO = createTestEventDTOAdd();
        eventService.addEvent(eventMapperAdd.map(eventDTO));
        long eventId = eventService.getEvents(null, null, null, null, null, null, null).get(0).getId();

        given()
                .when()
                .delete("/events/delete/" + eventId)
                .then()
                .statusCode(200);

        given()
                .when()
                .get("/events/getById/" + eventId)
                .then()
                .statusCode(404);
    }

    @Test
    void deleteEvent_shouldReturnNotFoundForNonExistentId() {
        given()
                .when()
                .delete("/events/delete/999")
                .then()
                .statusCode(404);
    }

    @Test
    void markAsComplete_shouldChangeStatus() {
        EventDTOAdd eventDTO = createTestEventDTOAdd();
        eventService.addEvent(eventMapperAdd.map(eventDTO));
        long eventId = eventService.getEvents(null, null, null, null, null, null, null).get(0).getId();

        given()
                .when()
                .put("/events/markAsComplete/" + eventId)
                .then()
                .statusCode(200);

        assertEquals(Status.Completed, eventService.getById(eventId).getStatus());
    }

    @Test
    void markAsUnComplete_shouldChangeStatus() {
        EventDTOAdd eventDTO = createTestEventDTOAdd();
        eventService.addEvent(eventMapperAdd.map(eventDTO));
        long eventId = eventService.getEvents(null, null, null, null, null, null, null).get(0).getId();
        eventService.markAsComplete(eventId);

        given()
                .when()
                .put("/events/markAsUnComplete/" + eventId)
                .then()
                .statusCode(200);

        assertEquals(Status.Active, eventService.getById(eventId).getStatus());
    }

    @Test
    void getEventById_shouldReturnEvent() {
        EventDTOAdd eventDTO = createTestEventDTOAdd();
        eventService.addEvent(eventMapperAdd.map(eventDTO));
        long eventId = eventService.getEvents(null, null, null, null, null, null, null).get(0).getId();

        given()
                .when()
                .get("/events/getById/" + eventId)
                .then()
                .statusCode(200)
                .body("title", equalTo("Test Event"))
                .body("id", equalTo((int) eventId));
    }

    @Test
    void getEventById_shouldReturnNotFoundForNonExistentId() {
        given()
                .when()
                .get("/events/getById/999")
                .then()
                .statusCode(404);
    }

    @Test
    void shouldHandleMultipleOperations() {
        EventDTOAdd eventDTO = createTestEventDTOAdd();
        given()
                .contentType(ContentType.JSON)
                .body(eventDTO)
                .when()
                .post("/events/create")
                .then()
                .statusCode(200);

        long eventId = eventService.getEvents(null, null, null, null, null, null, null).get(0).getId();

        EventDTOEdit updatedEvent = createTestEventDTOEdit();
        given()
                .contentType(ContentType.JSON)
                .body(updatedEvent)
                .when()
                .put("/events/edit/" + eventId)
                .then()
                .statusCode(200);

        given()
                .when()
                .get("/events/getById/" + eventId)
                .then()
                .statusCode(200)
                .body("title", equalTo("Updated Event"));

        given()
                .when()
                .delete("/events/delete/" + eventId)
                .then()
                .statusCode(200);

        given()
                .when()
                .get("/events/getById/" + eventId)
                .then()
                .statusCode(404);
    }
}