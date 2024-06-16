package com.marindulja.mentalhealthbackend.dtos.task;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TaskCompletionMoodDto {
    private Long assignedToUserId;
    private Double completionRate;
    private Double avgMoodLevel;
}
