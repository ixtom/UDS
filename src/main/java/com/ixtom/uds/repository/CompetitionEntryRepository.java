package com.ixtom.uds.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ixtom.uds.model.CompetitionEntry;
import com.ixtom.uds.model.CompetitionEntry.CompetitionEntryId;

public interface CompetitionEntryRepository extends JpaRepository<CompetitionEntry, CompetitionEntryId> {
    public List<CompetitionEntry> findByCompetitionId(Long competitionId);
    public List<CompetitionEntry> findByDuelistId(Long duelistId);
}
