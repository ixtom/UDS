package com.ixtom.uds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.ixtom.uds.model.Competition;
import com.ixtom.uds.model.CompetitionEntry;
import com.ixtom.uds.model.CompetitionEntry.CompetitionEntryId;
import com.ixtom.uds.model.Duel;
import com.ixtom.uds.model.Duelist;
import com.ixtom.uds.model.UserAuth;
import com.ixtom.uds.model.UserAuth.Role;
import com.ixtom.uds.model.UserProfile;

import jakarta.persistence.EntityManager;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DuelistHistoryTest {
    @Autowired private MockMvc mvc;
    @Autowired private EntityManager em;

    @Test
    void anonymousVisitorSeesBothSidesOfMatchupsAndDistinctCompetitions() throws Exception {
        Duelist ash = duelist("Ash");
        Duelist bolt = duelist("Bolt");
        Duelist cedar = duelist("Cedar");
        Competition cup = competition("Autumn Cup", Instant.parse("2025-11-01T00:00:00Z"));
        Competition upcoming = competition("Spring Cup", Instant.parse("2099-03-01T00:00:00Z"));
        Competition historical = competition("Old Cup", Instant.parse("2024-11-01T00:00:00Z"));
        enter(cup, ash);
        enter(upcoming, ash);

        Duel firstRound = duel(ash, bolt, cup, 1, null);
        Duel secondRound = duel(cedar, ash, cup, 2, "Second-round animation");
        Duel historicalDuel = duel(ash, cedar, historical, null, "Old matchup");
        Duel unrelated = duel(bolt, cedar, cup, 2, "Unrelated matchup");
        flushAndClear();

        String html = mvc.perform(get("/duelist/{id}", ash.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Round 1: Ash vs Bolt")))
                .andExpect(content().string(containsString("Second-round animation")))
                .andExpect(content().string(containsString("vs Cedar")))
                .andExpect(content().string(containsString("round 2")))
                .andExpect(content().string(containsString(duelLink(historicalDuel))))
                .andExpect(content().string(not(containsString(duelLink(unrelated)))))
                .andReturn().getResponse().getContentAsString();

        assertThat(html).containsSubsequence(duelLink(secondRound), duelLink(firstRound));
        assertThat(StringUtils.countOccurrencesOf(html, competitionLink(cup))).isEqualTo(1);
        assertThat(html).containsSubsequence(competitionLink(upcoming), "Spring Cup", "Registered");
        assertThat(html).containsSubsequence(competitionLink(cup), "Autumn Cup", "Matchup recorded");
        assertThat(html).containsSubsequence(competitionLink(historical), "Old Cup", "Matchup recorded");

        mvc.perform(get("/duelpage/{id}", secondRound.getId())).andExpect(status().isOk());
        mvc.perform(get("/competitions/{id}", cup.getId())).andExpect(status().isOk());
    }

    @Test
    void olderDuelsCanOmitOneDuelistAndAccountOnlyDuelsAreNotAttributed() throws Exception {
        Duelist ash = duelist("Ash");
        Duel firstSide = duel(ash, null, null, null, null);
        Duel secondSide = duel(null, ash, null, null, null);

        Duel accountOnly = new Duel();
        accountOnly.setUser1(ash.getOwner());
        accountOnly.setTitle("Account-only duel");
        em.persist(accountOnly);
        flushAndClear();

        mvc.perform(get("/duelist/{id}", ash.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(duelLink(firstSide))))
                .andExpect(content().string(containsString(duelLink(secondSide))))
                .andExpect(content().string(containsString("Ash vs TBD")))
                .andExpect(content().string(containsString("TBD vs Ash")))
                .andExpect(content().string(containsString("Standalone duel")))
                .andExpect(content().string(containsString("No competitions yet.")))
                .andExpect(content().string(not(containsString(duelLink(accountOnly)))));

        mvc.perform(get("/duelpage/{id}", secondSide.getId())).andExpect(status().isOk());
    }

    @Test
    void duelistWithoutHistoryShowsBothEmptyStates() throws Exception {
        Duelist ash = duelist("Ash");
        flushAndClear();

        mvc.perform(get("/duelist/{id}", ash.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No duels yet.")))
                .andExpect(content().string(containsString("No competitions yet.")));
    }

    @Test
    void unknownDuelistReturnsNotFound() throws Exception {
        mvc.perform(get("/duelist/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    private Duelist duelist(String name) {
        UserAuth account = new UserAuth(name + "@example.com", "unused-by-this-test", Role.USER);
        UserProfile profile = new UserProfile(null, name + "Owner");
        account.setProfile(profile);
        profile.setUserAuth(account);
        em.persist(account);

        Duelist duelist = new Duelist(name, "", "Backstory", "abcdefghijk", null);
        duelist.setOwner(profile);
        em.persist(duelist);
        return duelist;
    }

    private Competition competition(String name, Instant start) {
        Competition competition = new Competition(name, start, 4);
        em.persist(competition);
        return competition;
    }

    private void enter(Competition competition, Duelist duelist) {
        CompetitionEntry entry = new CompetitionEntry();
        entry.setId(new CompetitionEntryId());
        entry.setCompetition(competition);
        entry.setDuelist(duelist);
        em.persist(entry);
    }

    private Duel duel(Duelist first, Duelist second, Competition competition, Integer round, String title) {
        Duel duel = new Duel();
        duel.setDuelist1(first);
        duel.setDuelist2(second);
        duel.setCompetition(competition);
        duel.setRoundnum(round);
        duel.setTitle(title);
        em.persist(duel);
        return duel;
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    private String duelLink(Duel duel) {
        return "href=\"/duelpage/" + duel.getId() + "\"";
    }

    private String competitionLink(Competition competition) {
        return "href=\"/competitions/" + competition.getId() + "\"";
    }
}
