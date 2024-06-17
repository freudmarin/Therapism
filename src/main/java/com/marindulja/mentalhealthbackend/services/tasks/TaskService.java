package com.marindulja.mentalhealthbackend.services.tasks;

import com.marindulja.mentalhealthbackend.dtos.task.AssignedTaskDto;
import com.marindulja.mentalhealthbackend.dtos.task.TaskCompletionMoodDto;
import com.marindulja.mentalhealthbackend.dtos.task.TaskDto;
import com.marindulja.mentalhealthbackend.models.TaskStatus;

import java.util.List;

public interface TaskService {

    List<AssignedTaskDto> allTasksAssignedToPatient();

    List<AssignedTaskDto> allTasksAssignedByTherapist();

    AssignedTaskDto assignTaskToPatient(Long patientId, TaskDto taskDto);

    AssignedTaskDto updatePatientTask(Long patientId, Long taskId, TaskDto taskDto);

    AssignedTaskDto changeTaskStatus(Long taskId, TaskStatus newTaskStatus);

    List<TaskCompletionMoodDto> getTaskCompletionAndMoodByPatientId(Long patientId);
}
