package org.service.todo.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.service.todo.model.Event;
import org.service.todo.model.Priority;
import org.service.todo.model.Status;
import org.service.todo.repository.Repository;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Validated
@Service
public class EventService {

    private final Repository repository;

    public EventService(Repository repository) {
        this.repository = repository;
    }

    public List<Event> getEvents(String title, String description, LocalDate deadline, Status status, Priority priority, LocalDate creationDate, LocalDate editDate) {
        List<Event> events;
        if (title != null) {
            events = repository.findByOrderByTitleAsc();
        } else if (description != null) {
            events = repository.findByOrderByDescriptionAsc();
        } else if (deadline != null) {
            events = repository.findByOrderByDeadlineAsc();
        } else if (status != null) {
            events = repository.findByOrderByStatusAsc();
        } else if (priority != null) {
            events = repository.findByOrderByPriorityAsc();
        } else if (creationDate != null) {
            events = repository.findByOrderByCreatedDateAsc();
        } else if (editDate != null) {
            events = repository.findByOrderByEditDateAsc();
        } else {
            events = repository.findAll();
        }
        for (Event event : events) {
            if (event.getDeadline() != null && event.getDeadline().isBefore(LocalDate.now()) && event.getStatus() != Status.Completed && event.getStatus() != Status.Late) {
                event.setStatus(Status.Overdue);
                Event task = repository.findById(event.getId()).orElse(null);
                if (task != null) {
                    task.setStatus(Status.Overdue);
                    repository.save(task);
                }
            } else if (event.getDeadline() == null) {
                event.setStatus(Status.Active);
                Event task = repository.findById(event.getId()).orElse(null);
                if (task != null) {
                    task.setStatus(Status.Active);
                    repository.save(task);
                }
            }
        }
        return events;
    }

    public void addEvent(@Valid Event event) {
        repository.save(makeMakPriority(makeMakDate(event)));
    }

    public Event makeMakPriority(Event event) {

        Pattern priorityPattern = Pattern.compile("!([1234])");
        Pattern priorityPatternWrong = Pattern.compile("!(\\d+)");
        Matcher priorityMatcher = priorityPattern.matcher(event.getTitle());

        if (priorityMatcher.find() && event.getPriority() == null) {
            String priority = event.getTitle().substring(priorityMatcher.start(), priorityMatcher.end());
            switch (priority) {
                case "!1" -> event.setPriority(Priority.Critical);
                case "!2" -> event.setPriority(Priority.High);
                case "!4" -> event.setPriority(Priority.Low);
                default -> event.setPriority(Priority.Medium);
            }
        } else if (event.getPriority() == null) {
            event.setPriority(Priority.Medium);
        }
        event.setTitle(event.getTitle().replaceAll(priorityPattern.pattern(), ""));
        event.setTitle(event.getTitle().replaceAll(priorityPatternWrong.pattern(), ""));
        return event;
    }

    public Event makeMakDate(Event event) {

        Pattern datePattern = Pattern.compile("!before (\\d{2}-\\d{2}-\\d{4}|\\d{2}\\.\\d{2}\\.\\d{4})");
        Pattern datePatternWrong = Pattern.compile("!before (\\d{2}.\\d{2}.\\d{4})");

        Matcher dateMatcher = datePattern.matcher(event.getTitle());
        LocalDate dateTime = null;

        if (dateMatcher.find() && event.getDeadline() == null) {
            String date = event.getTitle().substring(dateMatcher.start() + 8, dateMatcher.end());

            try {
                if (dateMatcher.group(1).contains("-")) {
                    dateTime = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                } else {
                    dateTime = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                }
            } catch (Exception ignored) {
            }
            event.setDeadline(dateTime);
        }
        event.setTitle(event.getTitle().replaceAll(datePattern.pattern(), ""));
        event.setTitle(event.getTitle().replaceAll(datePatternWrong.pattern(), ""));
        return event;
    }

    public void editEvent(Event event, Long id) {
        Event task = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Event not found"));
        if (!event.getTitle().equals(task.getTitle())) {
            task.setTitle(event.getTitle());
        }
        if (!event.getDescription().equals(task.getDescription())) {
            task.setDescription(event.getDescription());
        }
        if (event.getDeadline() != null && !event.getDeadline().equals(task.getDeadline())) {
            task.setDeadline(event.getDeadline());
        }
        if (!event.getPriority().equals(task.getPriority())) {
            task.setPriority(event.getPriority());
        }
        repository.save(task);

    }

    public void deleteEvent(long id) {
        Event task = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Event not found"));
        repository.deleteById(id);

    }

    public void markAsComplete(long id) {
        Event event = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Event not found"));
        if (event.getStatus() == Status.Overdue) {
            event.setStatus(Status.Late);
        } else {
            event.setStatus(Status.Completed);
        }
        repository.save(event);

    }

    public void markAsUnComplete(long id) {
        Event event = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Event not found"));
        if (event != null) {
            if (event.getStatus() == Status.Late) {
                event.setStatus(Status.Overdue);
            } else {
                event.setStatus(Status.Active);
            }
            repository.save(event);
        }
    }

    public Event getById(Long id) {
        Event event = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Event not found"));
        if (event.getDeadline() != null && event.getDeadline().isBefore(LocalDate.now()) && event.getStatus() != Status.Completed && event.getStatus() != Status.Late) {
            event.setStatus(Status.Overdue);
            Event task = repository.findById(event.getId()).orElse(null);
            if (task != null) {
                task.setStatus(Status.Overdue);
                repository.save(task);
            }
        } else if (event.getDeadline() == null) {
            event.setStatus(Status.Active);
            Event task = repository.findById(event.getId()).orElse(null);
            if (task != null) {
                task.setStatus(Status.Active);
                repository.save(task);
            }
        } else if (event.getDeadline().isBefore(LocalDate.now()) && event.getStatus() != Status.Completed && event.getStatus() != Status.Late) {
            event.setStatus(Status.Overdue);
            Event task = repository.findById(event.getId()).orElse(null);
            if (task != null) {
                task.setStatus(Status.Overdue);
                repository.save(task);
            }
        } else if (event.getDeadline() != null && event.getDeadline().isAfter(LocalDate.now()) && event.getStatus() != Status.Completed && event.getStatus() != Status.Late) {
            event.setStatus(Status.Active);
            Event task = repository.findById(event.getId()).orElse(null);
            if (task != null) {
                task.setStatus(Status.Active);
                repository.save(task);
            }
        } else if (event.getDeadline() != null && event.getDeadline().isBefore(LocalDate.now()) && event.getStatus() == Status.Completed) {
            event.setStatus(Status.Late);
            Event task = repository.findById(event.getId()).orElse(null);
            if (task != null) {
                task.setStatus(Status.Late);
                repository.save(task);
            }
        } else if (event.getDeadline() != null && event.getDeadline().isAfter(LocalDate.now()) && event.getStatus() == Status.Late) {
            event.setStatus(Status.Completed);
            Event task = repository.findById(event.getId()).orElse(null);
            if (task != null) {
                task.setStatus(Status.Completed);
                repository.save(task);
            }
        }
        return repository.findById(id).orElse(null);
    }

    public void deleteAllEvents() {
        repository.deleteAll();
    }
}
