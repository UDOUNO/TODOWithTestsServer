package org.service.todo.specification;

import org.service.todo.model.Event;
import org.service.todo.model.Priority;
import org.service.todo.model.Status;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class EventSpec {

    private EventSpec(){}

    public static Specification<Event> filterByTitle(String title) {
        return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("title"), title));
    }

    public static Specification<Event> filterByDescription(String description) {
        return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("description"), description));
    }

    public static Specification<Event> filterByDeadline(LocalDate deadline) {
        return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("deadline"), deadline));
    }

    public static Specification<Event> filterByStatus(Status status) {
        return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status));
    }

    public static Specification<Event> filterByPriority(Priority priority) {
        return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("priority"), priority));
    }

    public static Specification<Event> filterByCreationDate(LocalDate creationDate) {
        return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("creationDate"), creationDate));
    }

    public static Specification<Event> filterByEditDate(LocalDate editDate) {
        return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("editDate"), editDate));
    }
}
