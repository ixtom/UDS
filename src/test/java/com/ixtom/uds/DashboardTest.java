package com.ixtom.uds;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

import com.ixtom.uds.model.Competition;
import com.ixtom.uds.model.UserAuth;

/**
 * Story 16's test table in the requirements doc is empty. These tests are derived from the
 * story text: the dashboard lets a user see what competitions they are in.
 */
class DashboardTest extends IntegrationTest {

    // Story 16 (no table row): a signed-in user sees the competitions their duelists entered
    @Test
    void dashboardListsUsersCompetitions() throws Exception {
        UserAuth user = createUser("benny");
        Competition competition = createCompetition("Winter Showdown",
            Instant.now().plus(7, ChronoUnit.DAYS), 3);
        enter(competition, createDuelist(user, "Shadow Blade"));
        flushAndClear();

        mvc.perform(get("/dashboard").with(as(user)))
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/index"));

        mvc.perform(get("/dashboard/competitions").with(as(user)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Winter Showdown")));
    }

    // Story 16 (no table row): the dashboard is private, logged-out visitors are sent to login
    @Test
    void loggedOutVisitorIsSentToLogin() throws Exception {
        mvc.perform(get("/dashboard"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }
}
