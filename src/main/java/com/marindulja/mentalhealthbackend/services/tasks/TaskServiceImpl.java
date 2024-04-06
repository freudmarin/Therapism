package com.marindulja.mentalhealthbackend.services.tasks;

import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.AssignedTaskDto;
import com.marindulja.mentalhealthbackend.dtos.TaskCompletionMoodDto;
import com.marindulja.mentalhealthbackend.dtos.TaskDto;
import com.marindulja.mentalhealthbackend.exceptions.InvalidInputException;
import com.marindulja.mentalhealthbackend.exceptions.UnauthorizedException;
import com.marindulja.mentalhealthbackend.models.Role;
import com.marindulja.mentalhealthbackend.models.Task;
import com.marindulja.mentalhealthbackend.models.TaskStatus;
import com.marindulja.mentalhealthbackend.repositories.ProfileRepository;
import com.marindulja.mentalhealthbackend.repositories.TaskRepository;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

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
        final var allTasksAssignedToPatient = taskRepository.getAllByAssignedToUser(Utilities.getCurrentUser().get());
        return allTasksAssignedToPatient.stream().map(task -> mapper.map(task, AssignedTaskDto.class)).collect(Collectors.toList());
    }

    @Override
    public List<AssignedTaskDto> allTasksAssignedByTherapist() {
        final var allTasksAssignedToPatient = taskRepository.getAllByAssignedByUser(Utilities.getCurrentUser().get());
        return allTasksAssignedToPatient.stream().map(task -> mapper.map(task, AssignedTaskDto.class)).collect(Collectors.toList());
    }

    @Override
    public AssignedTaskDto assignTaskToUser(Long patientId, TaskDto taskDto) {
        //even the therapist can be a patient
        if (Utilities.patientBelongsToTherapist(patientId, userProfileRepository)) {
            if (StringUtils.isBlank(taskDto.getDescription())) {
                throw new InvalidInputException("Task description cannot be null or empty");
            }
            final var taskToBeAssigned = mapper.map(taskDto, Task.class);
            taskToBeAssigned.setAssignedByUser(Utilities.getCurrentUser().get());
            taskToBeAssigned.setAssignedToUser(userProfileRepository.findByUserId(patientId).get().getUser());
            taskToBeAssigned.setStatus(TaskStatus.ASSIGNED);
            taskRepository.save(taskToBeAssigned);
            return mapper.map(taskToBeAssigned, AssignedTaskDto.class);
        }

        return null;
    }

    @Override
    public AssignedTaskDto updatePatientTask(Long patientId, Long taskId, TaskDto taskDto) {
        if (Utilities.patientBelongsToTherapist(patientId, userProfileRepository)) {
            final var existingTask = taskRepository.findById(taskId).orElseThrow(() -> new EntityNotFoundException("Task with id " + taskId + "not found"));
            existingTask.setAssignedByUser(Utilities.getCurrentUser().get());
            existingTask.setAssignedToUser(userProfileRepository.findByUserId(patientId).get().getUser());
            existingTask.setDescription(taskDto.getDescription());
            existingTask.setStatus(TaskStatus.REASSIGNED);
            taskRepository.save(existingTask);
            return mapper.map(existingTask, AssignedTaskDto.class);
        }
        return null;
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
        var currentUser = Utilities.getCurrentUser().get();
        if (currentUser.getRole() != Role.THERAPIST && currentUser.getRole() != Role.PATIENT)
            throw new UnauthorizedException("The role of current user should be Therapist or Patient");

        var results = taskRepository.findTaskCompletionAndMoodByUserId(patientId);
        var taskCompletionMoodDtos = results.stream()
                .map(result -> new TaskCompletionMoodDto(
                        (Long) result[0], // assignedToUserId
                        ((Number) result[1]).doubleValue(), // completionRate, safely cast to Number then to Double
                        ((Number) result[2]).doubleValue() // avgMoodLevel
                ))
                .collect(Collectors.toList());

        if (currentUser.getRole() == Role.THERAPIST) {
            if (Utilities.patientBelongsToTherapist(patientId, userProfileRepository))
                return taskCompletionMoodDtos;
        }
        if (currentUser.getRole() == Role.PATIENT) {
            if (!currentUser.getId().equals(patientId))
                throw new UnauthorizedException("You have provided the wrong patientId");

            if (Utilities.therapistBelongsToPatient(currentUser.getTherapist().getId(), userProfileRepository)) {
                return taskCompletionMoodDtos;
            }
        }
        return null;
    }
}
