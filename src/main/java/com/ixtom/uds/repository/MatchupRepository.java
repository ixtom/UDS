package com.ixtom.uds.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.ixtom.uds.model.Matchup;

public interface MatchupRepository extends JpaRepository<Matchup, Long> {
    List<Matchup> findByCompetitionId(Long competitionId);

    @Transactional
    void deleteByCompetitionId(Long competitionId);
}