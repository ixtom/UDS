package com.ixtom.uds.dto;

import com.ixtom.uds.model.Duel;
import com.ixtom.uds.model.Duelist;

public record UserDuelView(
    Duel duel,
    boolean ownsDuelist1
) {
    public Duelist duelist() {
        return ownsDuelist1 ? duel.getDuelist1() : duel.getDuelist2();
    }

    public Duelist opponent() {
        return ownsDuelist1 ? duel.getDuelist2() : duel.getDuelist1();
    }

    // Status: In progress, Voting, Win / Lose
    // Submission deadline
}
