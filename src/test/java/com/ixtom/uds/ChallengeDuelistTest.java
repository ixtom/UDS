package com.ixtom.uds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import com.ixtom.uds.model.Duel;
import com.ixtom.uds.model.Duelist;
import com.ixtom.uds.model.UserAuth;

class ChallengeDuelistTest extends IntegrationTest {

    // Story 22, Success row: sign in with a duelist, open another user's duelist, choose Challenge!, select own duelist, submit -> new duel outside a competition, page shows both duelists and owners
    @Test
    void challengeCreatesDuelOutsideCompetition() throws Exception {
        UserAuth mengna = createUser("mengna");
        Duelist mine = createDuelist(mengna, "Blaze");
        Duelist theirs = createDuelist(createUser("benny"), "Frost");
        flushAndClear();

        mvc.perform(get("/duelist/" + theirs.getId() + "/challenge").with(as(mengna)))
            .andExpect(status().isOk())
            .andExpect(view().name("duelist/challenge"))
            .andExpect(content().string(containsString("Blaze")));

        MvcResult result = mvc.perform(post("/duelist/" + theirs.getId() + "/challenge")
                .param("duelistId", mine.getId().toString())
                .with(as(mengna))
                .with(csrf()))
            .andExpect(redirectedUrlPattern("/duelpage/*"))
            .andReturn();

        flushAndClear();
        List<Duel> duels = duelRepo.findAll();
        assertThat(duels).hasSize(1);
        Duel duel = duels.get(0);
        assertThat(result.getResponse().getRedirectedUrl()).isEqualTo("/duelpage/" + duel.getId());
        assertThat(duel.getCompetition()).isNull();
        assertThat(duel.getVideoid1()).isEmpty();
        assertThat(duel.getVideoid2()).isEmpty();
        assertThat(duel.getDuelBanner()).isNull();

        mvc.perform(get("/duelpage/" + duel.getId()))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Blaze")))
            .andExpect(content().string(containsString("Frost")))
            .andExpect(content().string(containsString("mengna")))
            .andExpect(content().string(containsString("benny")));
    }

    // Story 22, Failure row: while signed in, open /duelist/{id}/challenge using the ID of a duelist you own -> 400, no duel created
    @Test
    void challengingOwnDuelistIsRejected() throws Exception {
        UserAuth mengna = createUser("mengna");
        Duelist mine = createDuelist(mengna, "Blaze");
        flushAndClear();

        mvc.perform(get("/duelist/" + mine.getId() + "/challenge").with(as(mengna)))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/duelist/" + mine.getId() + "/challenge")
                .param("duelistId", mine.getId().toString())
                .with(as(mengna))
                .with(csrf()))
            .andExpect(status().isBadRequest());

        assertThat(duelRepo.count()).isZero();
    }

    // Story 22, Failure row: while logged out, open /duelist/{id}/challenge for an existing duelist -> 401, no duel created
    @Test
    void loggedOutChallengeIsRejected() throws Exception {
        Duelist theirs = createDuelist(createUser("benny"), "Frost");
        flushAndClear();

        mvc.perform(get("/duelist/" + theirs.getId() + "/challenge"))
            .andExpect(status().isUnauthorized());

        assertThat(duelRepo.count()).isZero();
    }

    // Story 22 (acceptance criterion, not a table row): a user cannot challenge a duelist they own, so their own duelist page has no Challenge! link
    @Test
    void ownDuelistPageHasNoChallengeLink() throws Exception {
        UserAuth mengna = createUser("mengna");
        Duelist mine = createDuelist(mengna, "Blaze");
        flushAndClear();

        mvc.perform(get("/duelist/" + mine.getId()).with(as(mengna)))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString("/duelist/" + mine.getId() + "/challenge"))));
    }

    // Story 22 (precondition: the user owns the selected duelist; not a table row): picking someone else's duelist shows a form error, no duel created
    @Test
    void challengingWithSomeoneElsesDuelistIsRejected() throws Exception {
        UserAuth mengna = createUser("mengna");
        createDuelist(mengna, "Blaze");
        Duelist notMine = createDuelist(createUser("third_user"), "Ember");
        Duelist target = createDuelist(createUser("benny"), "Frost");
        flushAndClear();

        mvc.perform(post("/duelist/" + target.getId() + "/challenge")
                .param("duelistId", notMine.getId().toString())
                .with(as(mengna))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("duelist/challenge"))
            .andExpect(model().attributeHasFieldErrors("challengeDuelistRequest", "duelistId"));

        assertThat(duelRepo.count()).isZero();
    }

    // Story 22 (precondition: the user selects one of their duelists; not a table row): submitting without a duelist shows a form error, no duel created
    @Test
    void challengingWithoutChoosingDuelistIsRejected() throws Exception {
        UserAuth mengna = createUser("mengna");
        createDuelist(mengna, "Blaze");
        Duelist target = createDuelist(createUser("benny"), "Frost");
        flushAndClear();

        mvc.perform(post("/duelist/" + target.getId() + "/challenge")
                .with(as(mengna))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("duelist/challenge"))
            .andExpect(model().attributeHasFieldErrors("challengeDuelistRequest", "duelistId"));

        assertThat(duelRepo.count()).isZero();
    }
}
