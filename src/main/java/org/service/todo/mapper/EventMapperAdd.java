package org.service.todo.mapper;

import org.mapstruct.Mapper;
import org.service.todo.model.Event;
import org.service.todo.model.EventDTOAdd;

@Mapper(componentModel = "spring")
public interface EventMapperAdd {
    Event map(EventDTOAdd eventDTO);
}
