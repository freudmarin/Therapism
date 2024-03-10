package com.marindulja.mentalhealthbackend.dtos;

import com.marindulja.mentalhealthbackend.models.TaskStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskDto {
    private String description;
    private TaskStatus status;
}
