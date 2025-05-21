package org.service.todo.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class EventDTOAdd {
    @NotNull
    @Size(min = 4)
    private String title;

    private String description;

    private LocalDate deadline;

    private Priority priority;
}
