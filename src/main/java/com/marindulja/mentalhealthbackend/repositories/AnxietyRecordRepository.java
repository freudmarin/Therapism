package com.marindulja.mentalhealthbackend.repositories;

import com.marindulja.mentalhealthbackend.models.AnxietyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AnxietyRecordRepository extends JpaRepository<AnxietyRecord, Long>, JpaSpecificationExecutor<AnxietyRecord> {

}
