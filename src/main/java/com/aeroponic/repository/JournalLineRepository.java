package com.aeroponic.repository;

import com.aeroponic.model.ChartOfAccount;
import com.aeroponic.model.JournalEntry;
import com.aeroponic.model.JournalLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JournalLineRepository extends JpaRepository<JournalLine, Integer> {
    List<JournalLine> findByJournalEntry(JournalEntry journalEntry);
    List<JournalLine> findByAccount(ChartOfAccount account);

    @Query("SELECT jl FROM JournalLine jl JOIN FETCH jl.account JOIN FETCH jl.journalEntry ORDER BY jl.journalEntry.entryDate DESC")
    List<JournalLine> findAllWithDetails();
}
