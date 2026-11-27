package com.ixtom.uds.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.ixtom.uds.model.ImageAsset;

import jakarta.persistence.LockModeType;

public interface ImageAssetRepository extends JpaRepository<ImageAsset, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select image from ImageAsset image where image.id = :id")
    Optional<ImageAsset> findForUpdate(UUID id);

    List<ImageAsset> findTop20ByCleanupAfterLessThanEqualOrderByCleanupAfterAsc(Instant now);
}
