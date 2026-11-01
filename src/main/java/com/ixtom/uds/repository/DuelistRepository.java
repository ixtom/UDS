package com.ixtom.uds.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ixtom.uds.model.Duelist; 

public interface DuelistRepository extends JpaRepository<Duelist, Long> {

}