package org.service.todo.repository;

import org.service.todo.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface Repository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    List<Event> findByOrderByTitleAsc();

    List<Event> findByOrderByDescriptionAsc();

    List<Event> findByOrderByDeadlineAsc();

    List<Event> findByOrderByStatusAsc();

    List<Event> findByOrderByPriorityAsc();

    List<Event> findByOrderByCreatedDateAsc();

    List<Event> findByOrderByEditDateAsc();
}
