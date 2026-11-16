package com.ixtom.uds.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ixtom.uds.model.Duelist;

public interface DuelistRepository extends JpaRepository<Duelist, Long> {

    List<Duelist> findByOwnerIdOrderByNameAsc(Long ownerId);
    boolean existsByOwnerId(Long ownerId); // user has duelists
}
