package com.ixtom.uds.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ixtom.uds.model.Competition;

public interface CompetitionRepository extends JpaRepository<Competition, Long> {
    public List<Competition> findByParticipantsDuelistOwnerId(Long ownerId);

    @Query("SELECT c FROM Competition c ORDER BY c.start DESC NULLS LAST, c.id DESC")
    List<Competition> findAllByStartDateDescending();
}
