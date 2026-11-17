package com.ixtom.uds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.ixtom.uds.model.Competition;
import com.ixtom.uds.model.UserAuth;
import com.ixtom.uds.model.UserAuth.Role;

class CreateCompetitionTest extends IntegrationTest {

    private static final Instant FUTURE = Instant.now().plus(7, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
    private static final Instant PAST = Instant.now().minus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);

    // Story 17, Success row: as admin, open competitions, click "Create new competition", enter title and future start -> created, shown, listed
    @Test
    void adminCreatesCompetition() throws Exception {
        UserAuth admin = createUser("keith_admin", Role.ADMIN);

        mvc.perform(get("/competitions").with(as(admin)))
            .andExpect(content().string(containsString("Create new competition")));
        mvc.perform(get("/admin/competitions/new").with(as(admin)))
            .andExpect(status().isOk())
            .andExpect(view().name("competition/form"));

        mvc.perform(post("/admin/competitions/new")
                .param("title", "Winter Showdown 2027")
                .param("start", FUTURE.toString())
                .param("rounds", "3")
                .with(as(admin))
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("/admin/competitions/*"));

        flushAndClear();
        List<Competition> all = competitionRepo.findAll();
        assertThat(all).hasSize(1);
        Competition created = all.get(0);
        assertThat(created.getTitle()).isEqualTo("Winter Showdown 2027");
        assertThat(created.getStart()).isEqualTo(FUTURE);

        mvc.perform(get("/admin/competitions/" + created.getId()).with(as(admin)))
            .andExpect(content().string(containsString("Winter Showdown 2027")))
            .andExpect(content().string(containsString(FUTURE.toString())));
        mvc.perform(get("/competitions"))
            .andExpect(content().string(containsString("Winter Showdown 2027")));
    }

    // Story 17, Failure row: as admin, submit the create form with a blank title -> "Enter a title.", nothing created
    @Test
    void blankTitleIsRejected() throws Exception {
        UserAuth admin = createUser("keith_admin", Role.ADMIN);

        mvc.perform(post("/admin/competitions/new")
                .param("title", "")
                .param("start", FUTURE.toString())
                .param("rounds", "3")
                .with(as(admin))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("competition/form"))
            .andExpect(content().string(containsString("Enter a title.")));

        assertThat(competitionRepo.count()).isZero();
    }

    // Story 17, Failure row: as admin, submit the create form with a start date in the past -> "Start must be in the future.", nothing created
    @Test
    void pastStartIsRejected() throws Exception {
        UserAuth admin = createUser("keith_admin", Role.ADMIN);

        mvc.perform(post("/admin/competitions/new")
                .param("title", "Winter Showdown 2027")
                .param("start", PAST.toString())
                .param("rounds", "3")
                .with(as(admin))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("competition/form"))
            .andExpect(content().string(containsString("Start must be in the future.")));

        assertThat(competitionRepo.count()).isZero();
    }

    // Story 17, Failure row: as a regular user, go directly to /admin/competitions/new -> access denied, form not shown
    @Test
    void regularUserIsDenied() throws Exception {
        UserAuth user = createUser("regular");

        mvc.perform(get("/admin/competitions/new").with(as(user)))
            .andExpect(status().isForbidden());

        mvc.perform(post("/admin/competitions/new")
                .param("title", "Sneaky Cup")
                .param("start", FUTURE.toString())
                .param("rounds", "3")
                .with(as(user))
                .with(csrf()))
            .andExpect(status().isForbidden());
        assertThat(competitionRepo.count()).isZero();
    }

    // Story 17, Failure row: logged out, go directly to /admin/competitions/new -> sent to the login page
    @Test
    void loggedOutVisitorIsSentToLogin() throws Exception {
        mvc.perform(get("/admin/competitions/new"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }
}
