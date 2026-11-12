package com.ixtom.uds.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ixtom.uds.dto.DuelOpponentQuery;
import com.ixtom.uds.model.Duel;
import com.ixtom.uds.model.Duelist;

public interface DuelRepository extends JpaRepository<Duel, Long> {
    @Query("""
        SELECT CASE WHEN d.duelist1.id = :duelistId
            THEN d.duelist2 ELSE d.duelist1 END
        FROM Duel d
        WHERE d.competition.id = :competitionId
            AND d.roundnum = :roundNum
            AND (d.duelist1.id = :duelistId OR d.duelist2.id = :duelistId)
    """)
    Optional<Duelist> findOpponent(
        Long competitionId,
        Integer roundNum,
        Long duelistId
    );

    @Query("""
        SELECT d FROM Duel d
        WHERE d.duelist1.id = :duelistId
           OR d.duelist2.id = :duelistId
    """)
    List<Duel> findByDuelist(Long duelistId);

    List<Duel> findByCompetitionIdAndRoundnum(Long competitionId, Integer roundnum);

    @Query("""
        SELECT new com.ixtom.uds.dto.DuelOpponentQuery(d.id,
            CASE WHEN d.duelist1.owner.id = :userId
                THEN d.duelist2 ELSE d.duelist1 END)
        FROM Duel d
        WHERE d.duelist1.owner.id = :userId OR d.duelist2.owner.id = :userId
    """)
    List<DuelOpponentQuery> findOpponentsByUserId(Long userId);
}
