package org.service.todo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "event")
public class Event {
    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    @Size(min = 4)
    private String title;

    private String description;

    private LocalDate deadline;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Status status = Status.Active;

    @Enumerated(EnumType.STRING)
    private Priority priority;


    @CreatedDate
    private LocalDate createdDate;

    @LastModifiedDate
    private LocalDate editDate;
}
