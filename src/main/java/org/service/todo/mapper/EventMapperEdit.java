package org.service.todo.mapper;

import org.mapstruct.Mapper;
import org.service.todo.model.Event;
import org.service.todo.model.EventDTOEdit;

@Mapper(componentModel = "spring")
public interface EventMapperEdit {
    Event map(EventDTOEdit eventDTO);
}
