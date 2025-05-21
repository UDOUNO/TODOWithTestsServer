package org.service.todo.controller;

import jakarta.validation.Valid;
import org.service.todo.mapper.EventMapperAdd;
import org.service.todo.mapper.EventMapperEdit;
import org.service.todo.model.*;
import org.service.todo.service.EventService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
public class Controller {

    final EventService eventService;
    final EventMapperAdd eventMapperAdd;
    private final EventMapperEdit eventMapperEdit;

    public Controller(EventService eventService, EventMapperAdd eventMapperAdd, EventMapperEdit eventMapperEdit) {
        this.eventService = eventService;
        this.eventMapperAdd = eventMapperAdd;
        this.eventMapperEdit = eventMapperEdit;
    }

    @GetMapping("events/get")
    public List<Event> getEvents(@RequestParam(required = false) String title, @RequestParam(required = false) String description, @RequestParam(required = false) LocalDate deadline, @RequestParam(required = false) Status status, @RequestParam(required = false) Priority priority, @RequestParam(required = false) LocalDate creationDate, @RequestParam(required = false) LocalDate editDate){
        return eventService.getEvents(title,description,deadline,status,priority,creationDate,editDate);
    }

    @PostMapping("events/create")
    public void createEvent(@RequestBody @Valid EventDTOAdd eventDTO) {
        eventService.addEvent(eventMapperAdd.map(eventDTO));
    }

    @PutMapping("events/edit/{id}")
    public void editEvent(@RequestBody EventDTOEdit eventDTO, @PathVariable long id) {
        eventService.editEvent(eventMapperEdit.map(eventDTO), id);
    }

    @DeleteMapping("events/delete/{id}")
    public void deleteEvent(@PathVariable long id) {
        eventService.deleteEvent(id);
    }

    @DeleteMapping("events/delete/all")
    public void deleteAllEvents() {
        eventService.deleteAllEvents();
    }

    @PutMapping("events/markAsComplete/{id}")
    public void markAsComplete(@PathVariable long id) {
        eventService.markAsComplete(id);
    }

    @PutMapping("events/markAsUnComplete/{id}")
    public void markAsUnComplete(@PathVariable long id) {
        eventService.markAsUnComplete(id);
    }

    @GetMapping("events/getById/{id}")
    public Event getEventById(@PathVariable long id) {
        return eventService.getById(id);
    }
}
