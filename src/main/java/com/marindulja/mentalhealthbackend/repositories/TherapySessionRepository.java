package com.marindulja.mentalhealthbackend.repositories;

import com.marindulja.mentalhealthbackend.models.TherapySession;
import com.marindulja.mentalhealthbackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TherapySessionRepository extends JpaRepository<TherapySession, Long> {
    List<TherapySession> findTherapySessionsByTherapistAndSessionDateBetween(User therapist, LocalDateTime startDate, LocalDateTime endDate);


    @Query(value = "WITH SessionWithMood AS (" +
            "SELECT " +
            "s.patient_id, " +
            "s.session_date, " +
            "LAG(mj.mood_level) OVER (PARTITION BY s.patient_id ORDER BY mj.entry_date) AS mood_before, " +
            "mj.mood_level AS mood_day_of, " +
            "LEAD(mj.mood_level) OVER (PARTITION BY s.patient_id ORDER BY mj.entry_date) AS mood_after, " +
            "mj.entry_date " +
            "FROM " +
            "therapy_sessions s " +
            "INNER JOIN mood_journals mj ON s.patient_id = mj.user_id " +
            "WHERE " +
            "s.is_deleted = 0 AND mj.is_deleted = 0 AND s.patient_id = :patientId" + // Use :patientId parameter in WHERE clause
            ") " +
            "SELECT " +
            "patient_id, " +
            "session_date, " +
            "mood_before, " +
            "mood_day_of, " +
            "mood_after, " +
            "entry_date AS nearest_entry_date_before, " +
            "(" +
            "SELECT TOP 1 entry_date " +
            "FROM SessionWithMood sub " +
            "WHERE " +
            "sub.patient_id = swm.patient_id AND sub.entry_date > swm.session_date " +
            "ORDER BY sub.entry_date " +
            ") AS nearest_entry_date_after " +
            "FROM SessionWithMood swm " +
            "WHERE entry_date <= session_date " +
            "ORDER BY patient_id, session_date", nativeQuery = true)
    List<Object[]> findMoodChangesAroundTherapySessions(@Param("patientId") Long patientId);
}
