package com.ixtom.uds.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.ixtom.uds.model.UserProfile;

import jakarta.persistence.LockModeType;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUsername(String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select profile from UserProfile profile where profile.id = :id")
    Optional<UserProfile> findForUpdate(Long id);
}
