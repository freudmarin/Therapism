package com.marindulja.mentalhealthbackend.controllers;

import com.marindulja.mentalhealthbackend.dtos.AssignedTaskDto;
import com.marindulja.mentalhealthbackend.dtos.TaskDto;
import com.marindulja.mentalhealthbackend.models.TaskStatus;
import com.marindulja.mentalhealthbackend.services.tasks.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/tasks")
@PreAuthorize("hasAnyRole('PATIENT', 'THERAPIST')")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }


    @GetMapping("all")
    @PreAuthorize("hasAnyRole('PATIENT')")
    public ResponseEntity<List<AssignedTaskDto>> getAllTasksAssignedToUser() {
        List<AssignedTaskDto> allTasksAssignedToUser = taskService.allTasksAssignedToPatient();
        return new ResponseEntity<>(allTasksAssignedToUser, HttpStatus.OK);
    }

    @PutMapping("users/{userId}/assignTask")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<?> assignTaskToUser(@PathVariable Long userId, @RequestBody TaskDto request) {
        AssignedTaskDto assignedTask = taskService.assignTaskToUser(userId, request);
        return new ResponseEntity<>(assignedTask, HttpStatus.OK);
    }

    @PutMapping("users/{userId}/updateTask/{taskId}")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<?> updateExistingTask(@PathVariable Long userId, @PathVariable Long taskId, @RequestBody TaskDto request) {
        AssignedTaskDto updatedTask = taskService.updatePatientTask(userId, taskId, request);
        return new ResponseEntity<>(updatedTask, HttpStatus.OK);
    }

    @PatchMapping("{taskId}/status")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> updateTaskStatus(@PathVariable Long taskId, @RequestBody TaskDto taskDtoStatusUpdate) {
        AssignedTaskDto assignedTaskDto = taskService.changeTaskStatus(taskId,  TaskStatus.fromString(taskDtoStatusUpdate.getStatus()));
        return new ResponseEntity<>(assignedTaskDto, HttpStatus.OK);
    }
}








