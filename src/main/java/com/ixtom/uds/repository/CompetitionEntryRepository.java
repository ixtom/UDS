package com.ixtom.uds.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.ixtom.uds.model.CompetitionEntry;
import com.ixtom.uds.model.CompetitionEntry.CompetitionEntryId;

public interface CompetitionEntryRepository extends JpaRepository<CompetitionEntry, CompetitionEntryId> {
    public List<CompetitionEntry> findByCompetitionId(Long competitionId);
    public List<CompetitionEntry> findByDuelistId(Long duelistId);

    public boolean existsByCompetitionIdAndDuelistOwnerId(Long competitionId, Long ownerId);
    public long countByCompetitionId(Long competitionId);

    @Transactional
    public void deleteByCompetitionIdAndDuelistOwnerId(Long competitionId, Long userId);
}
