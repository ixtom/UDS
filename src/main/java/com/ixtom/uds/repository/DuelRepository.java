package com.ixtom.uds.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ixtom.uds.model.DuelData;

public interface DuelRepository extends JpaRepository<DuelData, Long> {
    
}
