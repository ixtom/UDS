package com.ixtom.uds.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ixtom.uds.model.UserProfile;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long>{
    Optional<UserProfile> findByUsername(String username);
}
