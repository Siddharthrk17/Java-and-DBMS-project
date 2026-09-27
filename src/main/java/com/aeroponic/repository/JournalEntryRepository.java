package com.aeroponic.repository;

import com.aeroponic.model.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Integer> {
    List<JournalEntry> findAllByOrderByEntryDateDesc();
    List<JournalEntry> findByJournalType(JournalEntry.JournalType journalType);
}
