package com.marindulja.mentalhealthbackend.services.tasks;

import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.task.AssignedTaskDto;
import com.marindulja.mentalhealthbackend.dtos.task.TaskCompletionMoodDto;
import com.marindulja.mentalhealthbackend.dtos.task.TaskDto;
import com.marindulja.mentalhealthbackend.exceptions.InvalidInputException;
import com.marindulja.mentalhealthbackend.exceptions.UnauthorizedException;
import com.marindulja.mentalhealthbackend.models.Role;
import com.marindulja.mentalhealthbackend.models.Task;
import com.marindulja.mentalhealthbackend.models.TaskStatus;
import com.marindulja.mentalhealthbackend.models.User;
import com.marindulja.mentalhealthbackend.repositories.ProfileRepository;
import com.marindulja.mentalhealthbackend.repositories.TaskRepository;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final ModelMapper mapper;

    private final TaskRepository taskRepository;

    private final ProfileRepository userProfileRepository;

    @Override
    public List<AssignedTaskDto> allTasksAssignedToPatient() {
        final var allTasksAssignedToPatient = taskRepository.getAllByAssignedToUser(getCurrentUserOrThrow());
        return allTasksAssignedToPatient.stream().map(task -> mapper.map(task, AssignedTaskDto.class)).collect(Collectors.toList());
    }

    @Override
    public List<AssignedTaskDto> allTasksAssignedByTherapist() {
        final var allTasksAssignedToPatient = taskRepository.getAllByAssignedByUser(getCurrentUserOrThrow());
        return allTasksAssignedToPatient.stream().map(task -> mapper.map(task, AssignedTaskDto.class)).collect(Collectors.toList());
    }

    @Override
    public AssignedTaskDto assignTaskToUser(Long patientId, TaskDto taskDto) {
        if (StringUtils.isBlank(taskDto.getDescription())) {
            throw new InvalidInputException("Task description cannot be null or empty");
        }
        final var therapist = getCurrentUserOrThrow();

        final var patientUser = userProfileRepository.findByUserId(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found for user ID: " + patientId))
                .getUser();
        if (!Utilities.patientBelongsToTherapist(patientId, userProfileRepository)) {
            throw new UnauthorizedException("Therapist not authorized to assign task to this patient");
        }

        final var taskToBeAssigned = mapper.map(taskDto, Task.class);
        taskToBeAssigned.setAssignedByUser(therapist);
        taskToBeAssigned.setAssignedToUser(patientUser);
        taskToBeAssigned.setStatus(TaskStatus.ASSIGNED);
        taskRepository.save(taskToBeAssigned);
        return mapper.map(taskToBeAssigned, AssignedTaskDto.class);
    }

    private User getCurrentUserOrThrow() {
        return Utilities.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("No authenticated user found"));
    }


    @Override
    @Transactional
    public AssignedTaskDto updatePatientTask(Long patientId, Long taskId, TaskDto taskDto) {
        final var currentUser = getCurrentUserOrThrow();
        if (!Utilities.patientBelongsToTherapist(patientId, userProfileRepository) && !currentUser.getId().equals(patientId)) {
            throw new UnauthorizedException("User is not authorized to update this task");
        }

        final var existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task with id " + taskId + " not found"));

        // Ensure the task is being reassigned to a user under the therapist's care
        if (!isReassignmentValid(currentUser, existingTask.getAssignedToUser().getId(), patientId)) {
            throw new UnauthorizedException("Task reassignment not valid or unauthorized");
        }

        updateTaskDetailsFromDto(existingTask, taskDto);
        final var updatedTask = taskRepository.save(existingTask);

        return mapper.map(updatedTask, AssignedTaskDto.class);
    }


    private boolean isReassignmentValid(User currentUser, Long originalAssignedUserId, Long newAssignedUserId) {
        // Validate that the therapist is reassigning the task within their patients or a patient is updating their own task
        return currentUser.getRole() == Role.THERAPIST && Utilities.patientBelongsToTherapist(newAssignedUserId, userProfileRepository)
                || currentUser.getId().equals(originalAssignedUserId) && originalAssignedUserId.equals(newAssignedUserId);
    }

    private void updateTaskDetailsFromDto(Task task, TaskDto taskDto) {
        // Assuming TaskDto contains fields that should update Task. Adjust according to your TaskDto.
        task.setDescription(taskDto.getDescription());
        task.setStatus(TaskStatus.REASSIGNED); // Consider checking if status change is valid based on business rules
    }

    @Override
    public AssignedTaskDto changeTaskStatus(Long taskId, TaskStatus newTaskStatus) {
        final var existingTask = taskRepository.findById(taskId).orElseThrow(() -> new EntityNotFoundException("Task with id " + taskId + "not found"));
        if (newTaskStatus.equals(TaskStatus.IN_PROGRESS) || newTaskStatus.equals(TaskStatus.COMPLETED)) {
            existingTask.setStatus(newTaskStatus);
            taskRepository.save(existingTask);
            return mapper.map(existingTask, AssignedTaskDto.class);
        }
        throw new IllegalArgumentException("Not appliable task status");
    }

    @Override
    public List<TaskCompletionMoodDto> getTaskCompletionAndMoodByPatientId(Long patientId) {
        final var currentUser = getCurrentUserOrThrow();

        validateUserAccessToPatientData(currentUser, patientId);

        final var results = taskRepository.findTaskCompletionAndMoodByUserId(patientId);

        return mapResultsToTaskCompletionMoodDtos(results);
    }

    private void validateUserAccessToPatientData(User currentUser, Long patientId) {
        if (currentUser.getRole() == Role.PATIENT && !currentUser.getId().equals(patientId)) {
            throw new UnauthorizedException("Patients can only access their own data");
        } else if (currentUser.getRole() == Role.THERAPIST && !Utilities.patientBelongsToTherapist(patientId, userProfileRepository)) {
            throw new UnauthorizedException("Therapists can only access data of their own patients");
        } else if (currentUser.getRole() != Role.THERAPIST && currentUser.getRole() != Role.PATIENT) {
            throw new UnauthorizedException("The role of current user should be Therapist or Patient");
        }
    }

    private List<TaskCompletionMoodDto> mapResultsToTaskCompletionMoodDtos(List<Object[]> results) {
        return results.stream()
                .map(result -> new TaskCompletionMoodDto(
                        (Long) result[0], // assignedToUserId
                        ((Number) result[1]).doubleValue(), // completionRate, safely cast to Number then to Double
                        ((Number) result[2]).doubleValue() // avgMoodLevel
                ))
                .collect(Collectors.toList());
    }
}
