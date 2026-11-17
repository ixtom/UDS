package com.ixtom.uds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;

import com.ixtom.uds.model.Duelist;
import com.ixtom.uds.model.UserAuth;

class ViewDuelistsTest extends IntegrationTest {

    // Story 12, Success row: open the profile of a user who owns multiple duelists -> only their duelists, alphabetical
    @Test
    void profileListsOnlyOwnDuelistsAlphabetically() throws Exception {
        UserAuth owner = createUser("jayden");
        createDuelist(owner, "Zephyr");
        createDuelist(owner, "Ash");
        createDuelist(owner, "Mika");
        createDuelist(createUser("someone_else"), "Bolt");
        flushAndClear();

        String html = mvc.perform(get("/profile/jayden"))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString("Bolt"))))
            .andReturn().getResponse().getContentAsString();

        int ash = html.indexOf(">Ash<");
        int mika = html.indexOf(">Mika<");
        int zephyr = html.indexOf(">Zephyr<");
        assertThat(ash).isPositive();
        assertThat(ash).isLessThan(mika);
        assertThat(mika).isLessThan(zephyr);
    }

    // Story 12, Success row: click a duelist's name in the profile -> the corresponding duelist page opens
    @Test
    void duelistNameLinksToDuelistPage() throws Exception {
        Duelist duelist = createDuelist(createUser("jayden"), "Ash");
        flushAndClear();

        mvc.perform(get("/profile/jayden"))
            .andExpect(content().string(containsString("href=\"/duelist/" + duelist.getId() + "\"")));

        mvc.perform(get("/duelist/" + duelist.getId()))
            .andExpect(status().isOk())
            .andExpect(view().name("duelist/view"))
            .andExpect(content().string(containsString("Ash")));
    }

    // Story 12, Success (empty state) row: open a profile belonging to a user with no duelists -> "No duelists yet" displayed
    @Test
    void userWithoutDuelistsShowsEmptyMessage() throws Exception {
        createUser("jayden");
        flushAndClear();

        mvc.perform(get("/profile/jayden"))
            .andExpect(content().string(containsString("No duelists yet")));
    }

    // Story 12, Failure row: open the profile of a nonexistent user -> 404 not-found response
    @Test
    void unknownUserReturnsNotFound() throws Exception {
        mvc.perform(get("/profile/ghost_user"))
            .andExpect(status().isNotFound());
    }
}
