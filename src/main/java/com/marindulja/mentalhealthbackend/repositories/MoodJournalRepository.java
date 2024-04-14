package com.marindulja.mentalhealthbackend.repositories;

import com.marindulja.mentalhealthbackend.models.MoodJournal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MoodJournalRepository extends JpaRepository<MoodJournal, Long> {
    // Additional query methods can be added if needed
    List<MoodJournal> findAllByUserId(Long userId);
}
