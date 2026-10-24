package com.ixtom.uds.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ixtom.uds.model.UserAuth;

public interface UserAuthRepository extends JpaRepository<UserAuth, Long>{
    Optional<UserAuth> findByEmail(String email);
    Optional<UserAuth> findByDiscordId(Long discordId);
}
