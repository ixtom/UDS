package com.ixtom.uds.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ixtom.uds.model.Competition;

public interface CompetitionRepository extends JpaRepository<Competition, Long> {
    public List<Competition> findByParticipantsDuelistOwnerId(Long ownerId);
}
