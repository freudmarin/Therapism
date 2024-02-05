package com.marindulja.mentalhealthbackend.repositories;

import com.marindulja.mentalhealthbackend.models.MoodJournal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MoodJournalRepository extends JpaRepository<MoodJournal, Long> {
    // Additional query methods can be added if needed
    List<MoodJournal> findAllByUserId(Long userId);
}
