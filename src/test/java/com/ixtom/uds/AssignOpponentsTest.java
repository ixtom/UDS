package com.ixtom.uds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.ixtom.uds.model.Competition;
import com.ixtom.uds.model.Duel;
import com.ixtom.uds.model.Duelist;
import com.ixtom.uds.model.UserAuth;
import com.ixtom.uds.model.UserAuth.Role;

/**
 * Stories 19 and 20 have empty test tables in the requirements doc. These tests are derived
 * from the story text: every participant gets an opponent, never a past opponent, and the
 * round's duel pages are created for the competitors to fill in.
 */
class AssignOpponentsTest extends IntegrationTest {

    private UserAuth admin;
    private Competition competition;
    private final List<Duelist> duelists = new ArrayList<>();

    private void startedCompetitionWithFourDuelists() {
        admin = createUser("keith_admin", Role.ADMIN);
        competition = createCompetition("Winter Showdown", Instant.now().minus(1, ChronoUnit.HOURS), 3);
        for (String name : List.of("Ash", "Blaze", "Cinder", "Dusk")) {
            Duelist duelist = createDuelist(createUser(name.toLowerCase() + "_owner"), name);
            enter(competition, duelist);
            duelists.add(duelist);
        }
    }

    private void pair() throws Exception {
        flushAndClear();
        mvc.perform(post("/admin/competitions/" + competition.getId() + "/pair")
                .with(as(admin))
                .with(csrf()))
            .andExpect(redirectedUrl("/admin/competitions/" + competition.getId()));
        flushAndClear();
    }

    private static Set<Long> pairOf(Duel duel) {
        return Set.of(duel.getDuelist1().getId(), duel.getDuelist2().getId());
    }

    private Duel roundOneDuel(Duelist a, Duelist b) {
        Duel duel = createDuel(a, b);
        duel.setCompetition(competition);
        duel.setRoundnum(1);
        return duelRepo.save(duel);
    }

    // Story 19 (no table row): every participant is assigned exactly one opponent who is not themselves
    @Test
    void everyParticipantGetsOneOpponent() throws Exception {
        startedCompetitionWithFourDuelists();

        pair();

        List<Duel> round1 = duelRepo.findByCompetitionIdAndRoundnum(competition.getId(), 1);
        assertThat(round1).hasSize(2);
        List<Long> paired = new ArrayList<>();
        for (Duel duel : round1) {
            assertThat(duel.getDuelist1().getId()).isNotEqualTo(duel.getDuelist2().getId());
            paired.add(duel.getDuelist1().getId());
            paired.add(duel.getDuelist2().getId());
        }
        assertThat(paired).containsExactlyInAnyOrderElementsOf(duelists.stream().map(Duelist::getId).toList());
    }

    // Story 19 (no table row; story text): the opponent assigned must not be one the participant had in the past
    @Test
    void secondRoundAvoidsRepeatOpponents() throws Exception {
        startedCompetitionWithFourDuelists();
        Duel previous1 = roundOneDuel(duelists.get(0), duelists.get(1)); // Ash vs Blaze
        Duel previous2 = roundOneDuel(duelists.get(2), duelists.get(3)); // Cinder vs Dusk
        Set<Set<Long>> pastPairs = Set.of(pairOf(previous1), pairOf(previous2));

        pair();

        List<Duel> round2 = duelRepo.findByCompetitionIdAndRoundnum(competition.getId(), 2);
        assertThat(round2).hasSize(2);
        for (Duel duel : round2) {
            assertThat(pastPairs).doesNotContain(pairOf(duel));
        }
    }

    // Story 19 (no table row): opponents are not assigned before the competition starts
    @Test
    void noPairingBeforeCompetitionStarts() throws Exception {
        admin = createUser("keith_admin", Role.ADMIN);
        competition = createCompetition("Future Cup", Instant.now().plus(7, ChronoUnit.DAYS), 3);
        enter(competition, createDuelist(createUser("ash_owner"), "Ash"));
        enter(competition, createDuelist(createUser("blaze_owner"), "Blaze"));

        pair();

        assertThat(duelRepo.findByCompetitionIdOrderByRoundnum(competition.getId())).isEmpty();
    }

    // Story 19 (no table row): only administrators can assign opponents
    @Test
    void regularUserCannotAssignOpponents() throws Exception {
        startedCompetitionWithFourDuelists();
        UserAuth user = createUser("regular");
        flushAndClear();

        mvc.perform(post("/admin/competitions/" + competition.getId() + "/pair")
                .with(as(user))
                .with(csrf()))
            .andExpect(status().isForbidden());

        assertThat(duelRepo.findByCompetitionIdOrderByRoundnum(competition.getId())).isEmpty();
    }

    // Story 20 (no table row): duel pages are created per round, numbered, and ready for the competitors to fill in
    @Test
    void duelPagesAreCreatedForTheRound() throws Exception {
        startedCompetitionWithFourDuelists();

        pair();

        List<Duel> round1 = duelRepo.findByCompetitionIdAndRoundnum(competition.getId(), 1);
        assertThat(round1).hasSize(2);
        for (Duel duel : round1) {
            assertThat(duel.getCompetition().getId()).isEqualTo(competition.getId());
            assertThat(duel.getUser1().getId()).isEqualTo(duel.getDuelist1().getOwner().getId());
            assertThat(duel.getUser2().getId()).isEqualTo(duel.getDuelist2().getOwner().getId());
            assertThat(duel.getVideoid1()).isEmpty();
            assertThat(duel.getVideoid2()).isEmpty();

            mvc.perform(get("/duelpage/" + duel.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Round 1: ")));
        }
    }

    // Story 20 (no table row): no duel pages are created past the competition's last round
    @Test
    void noDuelPagesPastLastRound() throws Exception {
        admin = createUser("keith_admin", Role.ADMIN);
        competition = createCompetition("Short Cup", Instant.now().minus(1, ChronoUnit.HOURS), 2);
        Duelist ash = createDuelist(createUser("ash_owner"), "Ash");
        Duelist blaze = createDuelist(createUser("blaze_owner"), "Blaze");
        enter(competition, ash);
        enter(competition, blaze);
        Duel last = createDuel(ash, blaze);
        last.setCompetition(competition);
        last.setRoundnum(2);
        duelRepo.save(last);

        pair();

        assertThat(duelRepo.findByCompetitionIdOrderByRoundnum(competition.getId())).hasSize(1);
    }
}
