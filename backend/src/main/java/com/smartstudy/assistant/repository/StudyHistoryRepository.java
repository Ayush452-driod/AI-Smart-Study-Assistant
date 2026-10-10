package com.smartstudy.assistant.repository;

import com.smartstudy.assistant.model.StudyHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudyHistoryRepository extends JpaRepository<StudyHistory, Long> {
    List<StudyHistory> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);
}
