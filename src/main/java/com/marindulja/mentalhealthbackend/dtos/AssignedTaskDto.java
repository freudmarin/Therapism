package com.marindulja.mentalhealthbackend.dtos;

import com.marindulja.mentalhealthbackend.models.TaskStatus;
import com.marindulja.mentalhealthbackend.models.User;
import lombok.Getter;

@Getter
public class AssignedTaskDto {
    private Long id;

    private User assignedByUser;

    private User assignedToUser;

    private String description;

    private TaskStatus status;
}
