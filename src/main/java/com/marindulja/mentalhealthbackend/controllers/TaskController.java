package com.marindulja.mentalhealthbackend.controllers;

import com.marindulja.mentalhealthbackend.dtos.task.AssignedTaskDto;
import com.marindulja.mentalhealthbackend.dtos.task.TaskCompletionMoodDto;
import com.marindulja.mentalhealthbackend.dtos.task.TaskDto;
import com.marindulja.mentalhealthbackend.services.tasks.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @GetMapping("all/patient")
    @PreAuthorize("hasAnyRole('PATIENT')")
    public ResponseEntity<List<AssignedTaskDto>> getAllTasksAssignedToPatient() {
        final var allTasksAssignedToPatient = taskService.allTasksAssignedToPatient();
        return new ResponseEntity<>(allTasksAssignedToPatient, HttpStatus.OK);
    }

    @GetMapping("all/therapist")
    @PreAuthorize("hasAnyRole('THERAPIST')")
    public ResponseEntity<List<AssignedTaskDto>> getAllTasksAssignedByTherapist() {
        final var allTasksAssignedByTherapist = taskService.allTasksAssignedByTherapist();
        return new ResponseEntity<>(allTasksAssignedByTherapist, HttpStatus.OK);
    }

    @PostMapping("users/{userId}/assignTask")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<?> assignTaskToUser(@PathVariable Long userId, @RequestBody TaskDto request) {
        final var assignedTask = taskService.assignTaskToUser(userId, request);
        return new ResponseEntity<>(assignedTask, HttpStatus.OK);
    }

    @PutMapping("users/{userId}/updateTask/{taskId}")
    @PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<?> updateExistingTask(@PathVariable Long userId, @PathVariable Long taskId, @RequestBody TaskDto request) {
        final var updatedTask = taskService.updatePatientTask(userId, taskId, request);
        return new ResponseEntity<>(updatedTask, HttpStatus.OK);
    }

    @PatchMapping("{taskId}/status")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> updateTaskStatus(@PathVariable Long taskId, @RequestBody TaskDto taskDtoStatusUpdate) {
        final var assignedTaskDto = taskService.changeTaskStatus(taskId, taskDtoStatusUpdate.getStatus());
        return new ResponseEntity<>(assignedTaskDto, HttpStatus.OK);
    }

    @GetMapping("moodImprovement/{patientId}")
    @PreAuthorize("hasAnyRole('THERAPIST','PATIENT')")
    public ResponseEntity<List<TaskCompletionMoodDto>> findCorrelationBetweenTaskCompletionAndMoodImprovement(@PathVariable Long patientId) {
        var moodImprovementAndTaskCompletion = taskService.getTaskCompletionAndMoodByPatientId(patientId);
        return new ResponseEntity<>(moodImprovementAndTaskCompletion, HttpStatus.OK);
    }
}








