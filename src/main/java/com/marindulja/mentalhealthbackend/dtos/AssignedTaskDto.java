package com.marindulja.mentalhealthbackend.dtos;

import com.marindulja.mentalhealthbackend.models.TaskStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignedTaskDto {
    private Long id;

    private UserDto assignedByUser;

    private UserDto assignedToUser;

    private String description;

    private TaskStatus status;
}
