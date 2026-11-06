package com.ixtom.uds.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ixtom.uds.model.AccountLink;
import com.ixtom.uds.model.AccountLink.AccountLinkId;

public interface AccountLinkRepository extends JpaRepository<AccountLink, AccountLinkId> {
    List<AccountLink> findByUserId(Long userId);

    @Query("""
        SELECT entry FROM AccountLink entry
        WHERE entry.id.provider = :provider
        AND entry.user.id = :userId
    """)
    Optional<AccountLink> findByProviderAndUserId(String provider, Long userId);

    @Query("""
        SELECT entry FROM AccountLink entry
        WHERE entry.id.provider = :provider
        AND entry.id.externalId = :externalId
    """)
    Optional<AccountLink> findByProviderAndExternalId(String provider, String externalId);
}
