package com.ixtom.uds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.ixtom.uds.model.Competition;
import com.ixtom.uds.model.UserAuth;
import com.ixtom.uds.model.UserAuth.Role;

class AdminDashboardTest extends IntegrationTest {
    private static final String DASHBOARD = "/admin/dashboard";
    private static final String DASHBOARD_LINK = "href=\"" + DASHBOARD + "\"";

    @Test
    void adminCanOpenExistingWorkflowsFromDashboard() throws Exception {
        UserAuth admin = createUser("admin", Role.ADMIN);
        Competition undated = createCompetition("Archive Cup", null, 3);
        Competition older = createCompetition("Autumn Cup", Instant.parse("2026-10-15T18:00:00Z"), 3);
        Competition newer = createCompetition("Winter Cup", Instant.parse("2027-01-15T18:00:00Z"), 3);
        String newerPath = "/admin/competitions/" + newer.getId();
        flushAndClear();

        String html = mvc.perform(get(DASHBOARD).with(as(admin)))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(content().string(containsString("datetime=\"2027-01-15T18:00:00Z\"")))
                .andReturn().getResponse().getContentAsString();

        assertThat(html).containsSubsequence(
                "Admin dashboard</h1>", "href=\"/admin/competitions/new\"", "href=\"/admin/users\"",
                "href=\"" + newerPath + "\"", "Winter Cup",
                "href=\"/admin/competitions/" + older.getId() + "\"", "Autumn Cup",
                "href=\"/admin/competitions/" + undated.getId() + "\"", "Archive Cup",
                "Start date unavailable");

        mvc.perform(get("/admin/users").with(as(admin))).andExpect(status().isOk());
        mvc.perform(get("/admin/competitions/new").with(as(admin))).andExpect(status().isOk());
        mvc.perform(get(newerPath).with(as(admin))).andExpect(status().isOk());
    }

    @Test
    void emptyDashboardOffersCompetitionCreation() throws Exception {
        UserAuth admin = createUser("admin", Role.ADMIN);

        mvc.perform(get(DASHBOARD).with(as(admin)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No competitions yet.")))
                .andExpect(content().string(containsString("href=\"/admin/competitions/new\"")));
    }

    @Test
    void onlyAdminsSeeDashboardInNavbar() throws Exception {
        UserAuth admin = createUser("admin", Role.ADMIN);
        UserAuth regular = createUser("regular");

        mvc.perform(get("/").with(as(admin)))
                .andExpect(content().string(containsString(DASHBOARD_LINK)));
        mvc.perform(get("/").with(as(regular)))
                .andExpect(content().string(not(containsString(DASHBOARD_LINK))));
        mvc.perform(get("/"))
                .andExpect(content().string(not(containsString(DASHBOARD_LINK))));
    }

    @Test
    void regularUserCannotOpenDashboardDirectly() throws Exception {
        UserAuth regular = createUser("regular");

        mvc.perform(get(DASHBOARD).with(as(regular)))
                .andExpect(status().isForbidden());
    }

    @Test
    void signedOutVisitorIsRedirectedToLogin() throws Exception {
        mvc.perform(get(DASHBOARD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
