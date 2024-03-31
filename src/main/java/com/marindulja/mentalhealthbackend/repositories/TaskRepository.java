package com.marindulja.mentalhealthbackend.repositories;

import com.marindulja.mentalhealthbackend.models.Task;
import com.marindulja.mentalhealthbackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> getAllByAssignedToUser(User patient);

    List<Task> getAllByAssignedByUser(User patient);

    @Query(value = "SELECT t.assigned_to_user_id AS assignedToUserId, " +
            "AVG(case when t.status = 4 then 1 else 0 end) AS completionRate, " +
            "AVG(mj.mood_level) AS avgMoodLevel " +
            "FROM tasks t " +
            "JOIN mood_journals mj ON t.assigned_to_user_id = mj.user_id " +
            "WHERE t.assigned_to_user_id = :assignedToUserId " +
            "GROUP BY t.assigned_to_user_id", nativeQuery = true)
    List<Object[]> findTaskCompletionAndMoodByUserId(@Param("assignedToUserId") Long assignedToUserId);
}
