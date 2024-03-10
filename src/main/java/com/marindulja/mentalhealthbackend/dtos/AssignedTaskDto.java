package com.marindulja.mentalhealthbackend.dtos;

import com.marindulja.mentalhealthbackend.models.TaskStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignedTaskDto {
    private Long id;

    private UserReadDto assignedByUser;

    private UserReadDto assignedToUser;

    private String description;

    private TaskStatus status;
}
