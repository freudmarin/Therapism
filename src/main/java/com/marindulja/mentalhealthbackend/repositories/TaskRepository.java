package com.marindulja.mentalhealthbackend.repositories;

import com.marindulja.mentalhealthbackend.models.Task;
import com.marindulja.mentalhealthbackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> getAllByAssignedToUser(User patient);
}
