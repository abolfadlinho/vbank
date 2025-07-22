package com.vbank.loggingservice.repository;

import com.vbank.loggingservice.model.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the LogEntry entity.
 */
@Repository
public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {
}