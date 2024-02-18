package com.marindulja.mentalhealthbackend.services.tasks;

import com.marindulja.mentalhealthbackend.dtos.AssignedTaskDto;
import com.marindulja.mentalhealthbackend.dtos.TaskDto;
import com.marindulja.mentalhealthbackend.models.TaskStatus;

import java.util.List;

public interface TaskService {

    List<AssignedTaskDto> allTasksAssignedToPatient();

    List<AssignedTaskDto> allTasksAssignedByTherapist();
    AssignedTaskDto assignTaskToUser(Long patientId, TaskDto taskDto);

    AssignedTaskDto updatePatientTask(Long patientId, Long taskId, TaskDto taskDto);

    AssignedTaskDto changeTaskStatus(Long taskId, TaskStatus newTaskStatus);
}
