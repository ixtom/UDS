package com.ixtom.uds.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.ixtom.uds.model.Duelist;

import jakarta.persistence.LockModeType;

public interface DuelistRepository extends JpaRepository<Duelist, Long> {

    List<Duelist> findByOwnerIdOrderByNameAsc(Long ownerId);
    boolean existsByOwnerId(Long ownerId); // user has duelists
    boolean existsByIdAndOwnerId(Long id, Long ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select duelist from Duelist duelist where duelist.id = :id")
    Optional<Duelist> findForUpdate(Long id);
}
