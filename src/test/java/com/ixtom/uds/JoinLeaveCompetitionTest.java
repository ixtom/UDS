package com.ixtom.uds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

import com.ixtom.uds.model.Competition;
import com.ixtom.uds.model.Duelist;
import com.ixtom.uds.model.UserAuth;

/**
 * Story 18's test table in the requirements doc is empty. These tests are derived from the
 * story text ("join or leave competitions") and the abstract ("join or leave it before it starts").
 */
class JoinLeaveCompetitionTest extends IntegrationTest {

    private Competition upcoming() {
        return createCompetition("Winter Showdown", Instant.now().plus(7, ChronoUnit.DAYS), 3);
    }

    private Competition alreadyStarted() {
        return createCompetition("Autumn Showdown", Instant.now().minus(1, ChronoUnit.DAYS), 3);
    }

    // Story 18 (no table row): a user with a duelist joins a competition that has not started
    @Test
    void userJoinsUpcomingCompetition() throws Exception {
        UserAuth user = createUser("benny");
        Duelist duelist = createDuelist(user, "Shadow Blade");
        Competition competition = upcoming();
        flushAndClear();

        mvc.perform(get("/competitions/" + competition.getId()).with(as(user)))
            .andExpect(model().attribute("canJoin", true));

        mvc.perform(post("/competitions/" + competition.getId() + "/join")
                .param("duelistId", duelist.getId().toString())
                .with(as(user))
                .with(csrf()))
            .andExpect(redirectedUrl("/competitions/" + competition.getId()));

        assertThat(competitionEntryRepo.existsByCompetitionIdAndDuelistOwnerId(competition.getId(), user.getId())).isTrue();
    }

    // Story 18 (no table row): a user leaves a competition before it starts
    @Test
    void userLeavesUpcomingCompetition() throws Exception {
        UserAuth user = createUser("benny");
        Competition competition = upcoming();
        enter(competition, createDuelist(user, "Shadow Blade"));
        flushAndClear();

        mvc.perform(post("/competitions/" + competition.getId() + "/leave")
                .with(as(user))
                .with(csrf()))
            .andExpect(redirectedUrl("/competitions/" + competition.getId()));

        assertThat(competitionEntryRepo.existsByCompetitionIdAndDuelistOwnerId(competition.getId(), user.getId())).isFalse();
    }

    // Story 18 (no table row): joining after the competition has started is refused
    @Test
    void joiningStartedCompetitionIsRefused() throws Exception {
        UserAuth user = createUser("benny");
        Duelist duelist = createDuelist(user, "Shadow Blade");
        Competition competition = alreadyStarted();
        flushAndClear();

        mvc.perform(post("/competitions/" + competition.getId() + "/join")
                .param("duelistId", duelist.getId().toString())
                .with(as(user))
                .with(csrf()))
            .andExpect(status().isConflict());

        assertThat(competitionEntryRepo.countByCompetitionId(competition.getId())).isZero();
    }

    // Story 18 (no table row): leaving after the competition has started is refused
    @Test
    void leavingStartedCompetitionIsRefused() throws Exception {
        UserAuth user = createUser("benny");
        Competition competition = alreadyStarted();
        enter(competition, createDuelist(user, "Shadow Blade"));
        flushAndClear();

        mvc.perform(post("/competitions/" + competition.getId() + "/leave")
                .with(as(user))
                .with(csrf()))
            .andExpect(status().isConflict());

        assertThat(competitionEntryRepo.existsByCompetitionIdAndDuelistOwnerId(competition.getId(), user.getId())).isTrue();
    }

    // Story 18 (no table row): joining with another user's duelist is rejected
    @Test
    void joiningWithSomeoneElsesDuelistIsRejected() throws Exception {
        UserAuth user = createUser("benny");
        createDuelist(user, "Shadow Blade");
        Duelist notMine = createDuelist(createUser("other_user"), "Frost");
        Competition competition = upcoming();
        flushAndClear();

        mvc.perform(post("/competitions/" + competition.getId() + "/join")
                .param("duelistId", notMine.getId().toString())
                .with(as(user))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors("joinCompetitionRequest", "duelistId"));

        assertThat(competitionEntryRepo.countByCompetitionId(competition.getId())).isZero();
    }
}
