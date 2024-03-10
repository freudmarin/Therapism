package com.marindulja.mentalhealthbackend.services.tasks;

import com.marindulja.mentalhealthbackend.common.Utilities;
import com.marindulja.mentalhealthbackend.dtos.AssignedTaskDto;
import com.marindulja.mentalhealthbackend.dtos.TaskDto;
import com.marindulja.mentalhealthbackend.exceptions.InvalidInputException;
import com.marindulja.mentalhealthbackend.exceptions.UnauthorizedException;
import com.marindulja.mentalhealthbackend.models.Task;
import com.marindulja.mentalhealthbackend.models.TaskStatus;
import com.marindulja.mentalhealthbackend.repositories.ProfileRepository;
import com.marindulja.mentalhealthbackend.repositories.TaskRepository;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    private final ModelMapper mapper;

    private final TaskRepository taskRepository;

    private final ProfileRepository userProfileRepository;

    public TaskServiceImpl(ModelMapper mapper, TaskRepository taskRepository, ProfileRepository userProfileRepository) {
        this.mapper = mapper;
        this.taskRepository = taskRepository;
        this.userProfileRepository = userProfileRepository;
    }
    @Override
    public List<AssignedTaskDto> allTasksAssignedToPatient() {
        final var allTasksAssignedToPatient = taskRepository.getAllByAssignedToUser(Utilities.getCurrentUser().get());
        return allTasksAssignedToPatient.stream().map(this::mapToAssignedTaskDto).collect(Collectors.toList());
    }

    @Override
    public List<AssignedTaskDto> allTasksAssignedByTherapist() {
        final var allTasksAssignedToPatient = taskRepository.getAllByAssignedByUser(Utilities.getCurrentUser().get());
        return allTasksAssignedToPatient.stream().map(this::mapToAssignedTaskDto).collect(Collectors.toList());
    }

    @Override
    public AssignedTaskDto assignTaskToUser(Long patientId, TaskDto taskDto) {
        //even the therapist can be a patient
        if (patientBelongsToTherapist(patientId)) {
            if (StringUtils.isBlank(taskDto.getDescription())) {
                throw new InvalidInputException("Task description cannot be null or empty");
            }
            final var taskToBeAssigned = mapToEntity(taskDto);
            taskToBeAssigned.setAssignedByUser(Utilities.getCurrentUser().get());
            taskToBeAssigned.setAssignedToUser(userProfileRepository.findByUserId(patientId).get().getUser());
            taskToBeAssigned.setStatus(TaskStatus.ASSIGNED);
            taskRepository.save(taskToBeAssigned);
            return mapToAssignedTaskDto(taskToBeAssigned);
        }

        return null;
    }

    @Override
    public AssignedTaskDto updatePatientTask(Long patientId, Long taskId, TaskDto taskDto) {
        if (patientBelongsToTherapist(patientId)) {
            final var existingTask = taskRepository.findById(taskId).orElseThrow(() -> new EntityNotFoundException("Task with id " + taskId + "not found"));
            existingTask.setAssignedByUser(Utilities.getCurrentUser().get());
            existingTask.setAssignedToUser(userProfileRepository.findByUserId(patientId).get().getUser());
            existingTask.setDescription(taskDto.getDescription());
            existingTask.setStatus(TaskStatus.REASSIGNED);
            taskRepository.save(existingTask);
            return mapToAssignedTaskDto(existingTask);
        }
        return null;
    }

    @Override
    public AssignedTaskDto changeTaskStatus(Long taskId, TaskStatus newTaskStatus) {
        final var existingTask = taskRepository.findById(taskId).orElseThrow(() -> new EntityNotFoundException("Task with id " + taskId + "not found"));
        if (newTaskStatus.equals(TaskStatus.IN_PROGRESS) || newTaskStatus.equals(TaskStatus.COMPLETED)) {
            existingTask.setStatus(newTaskStatus);
            taskRepository.save(existingTask);
            return mapToAssignedTaskDto(existingTask);
        }
        throw new IllegalArgumentException("Not appliable task status");
    }

    private boolean patientBelongsToTherapist(Long patientId) {
        final var therapist = Utilities.getCurrentUser().get();
        final var patientProfile = userProfileRepository.findByUserId(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient with id " + patientId + " not found"));

        if (patientProfile.getUser().getTherapist() == null || !therapist.getId().equals(patientProfile.getUser().getTherapist().getId())) {
            throw new UnauthorizedException("The patient with id " + patientId + " is not the patient of the therapist with id " + therapist.getId());
        }
        return true;
    }

    private TaskDto mapToDTO(Task task) {
        return mapper.map(task, TaskDto.class);
    }

    private Task mapToEntity(TaskDto taskDto) {
        return mapper.map(taskDto, Task.class);
    }

    private AssignedTaskDto mapToAssignedTaskDto(Task task) {
        return mapper.map(task, AssignedTaskDto.class);
    }

    private Task mapToAssignedTask(AssignedTaskDto assignedTaskDto) {
        return mapper.map(assignedTaskDto, Task.class);
    }
}
