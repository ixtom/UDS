package com.ixtom.uds.service;

import com.ixtom.uds.model.CompetitionEntry;
import com.ixtom.uds.model.Duel;
import com.ixtom.uds.repository.CompetitionEntryRepository;
import com.ixtom.uds.repository.DuelRepository;
import com.ixtom.uds.repository.VoteRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DuelScoringService {

    @Autowired private DuelRepository duelRepo;
    @Autowired private VoteRepository voteRepo;
    @Autowired private CompetitionEntryRepository entryRepo;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void scoreExpiredDuels() {
        List<Duel> expired = duelRepo.findByEndDateBeforeAndScoredFalse(Instant.now());
        for (Duel duel : expired) {
            finalizeDuelIfEnded(duel);
        }
    }

    @Transactional
    public void finalizeDuelIfEnded(Duel duel) {
        if (duel.isScored()
                || duel.getEndDate() == null
                || Instant.now().isBefore(duel.getEndDate())
                || duel.getCompetition() == null
                || duel.getDuelist1() == null
                || duel.getDuelist2() == null) {
            return;
        }

        Long duelId = duel.getId();
        Long compId = duel.getCompetition().getId();
        Long d1Id = duel.getDuelist1().getId();
        Long d2Id = duel.getDuelist2().getId();

        int d1Points = 0;
        int d2Points = 0;

        long d1Cat1 = voteRepo.countByDuelIdAndCat1winner(duelId, d1Id);
        long d2Cat1 = voteRepo.countByDuelIdAndCat1winner(duelId, d2Id);
        if (d1Cat1 > d2Cat1) d1Points++;
        else if (d2Cat1 > d1Cat1) d2Points++;

        long d1Cat2 = voteRepo.countByDuelIdAndCat2winner(duelId, d1Id);
        long d2Cat2 = voteRepo.countByDuelIdAndCat2winner(duelId, d2Id);
        if (d1Cat2 > d2Cat2) d1Points++;
        else if (d2Cat2 > d1Cat2) d2Points++;

        long d1Cat3 = voteRepo.countByDuelIdAndCat3winner(duelId, d1Id);
        long d2Cat3 = voteRepo.countByDuelIdAndCat3winner(duelId, d2Id);
        if (d1Cat3 > d2Cat3) d1Points++;
        else if (d2Cat3 > d1Cat3) d2Points++;

        long d1Cat4 = voteRepo.countByDuelIdAndCat4winner(duelId, d1Id);
        long d2Cat4 = voteRepo.countByDuelIdAndCat4winner(duelId, d2Id);
        if (d1Cat4 > d2Cat4) d1Points++;
        else if (d2Cat4 > d1Cat4) d2Points++;

        long d1Cat5 = voteRepo.countByDuelIdAndCat5winner(duelId, d1Id);
        long d2Cat5 = voteRepo.countByDuelIdAndCat5winner(duelId, d2Id);
        if (d1Cat5 > d2Cat5) d1Points++;
        else if (d2Cat5 > d1Cat5) d2Points++;

        final int finalD1Points = d1Points;
        final int finalD2Points = d2Points;

        entryRepo.findByCompetitionIdAndDuelistId(compId, d1Id).ifPresent(entry1 -> {
            entry1.setScore(entry1.getScore() + finalD1Points);
            entryRepo.save(entry1);
        });

        entryRepo.findByCompetitionIdAndDuelistId(compId, d2Id).ifPresent(entry2 -> {
            entry2.setScore(entry2.getScore() + finalD2Points);
            entryRepo.save(entry2);
        });

        duel.setScored(true);
        duelRepo.save(duel);
    }
}