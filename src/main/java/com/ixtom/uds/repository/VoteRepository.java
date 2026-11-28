package com.ixtom.uds.repository;

import com.ixtom.uds.model.Vote;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByDuelIdAndVoterId(Long duelId, Long voterId);
    List<Vote> findByDuelId(Long duelId);
    
    long countByDuelIdAndCat1winner(Long duelId, Long duelistId);
    long countByDuelIdAndCat2winner(Long duelId, Long duelistId);
    long countByDuelIdAndCat3winner(Long duelId, Long duelistId);
    long countByDuelIdAndCat4winner(Long duelId, Long duelistId);
    long countByDuelIdAndCat5winner(Long duelId, Long duelistId);
}
