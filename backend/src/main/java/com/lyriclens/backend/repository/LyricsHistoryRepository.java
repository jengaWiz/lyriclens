package com.lyriclens.backend.repository;

import com.lyriclens.backend.model.LyricsHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LyricsHistoryRepository extends JpaRepository<LyricsHistory, Long> {

}
