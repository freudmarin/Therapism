package com.marindulja.mentalhealthbackend.dtos.task;

import com.marindulja.mentalhealthbackend.models.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskDto {
    @NotBlank(message = "You should put a description for the task")
    private String description;
    private TaskStatus status;
}
