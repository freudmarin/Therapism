package com.marindulja.mentalhealthbackend.repositories;

import com.marindulja.mentalhealthbackend.models.TherapySession;
import com.marindulja.mentalhealthbackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TherapySessionRepository extends JpaRepository<TherapySession, Long> {
    List<TherapySession> getTherapySessionsByTherapistAndSessionDateBetween(User therapist, LocalDateTime startDate, LocalDateTime endDate);
}
