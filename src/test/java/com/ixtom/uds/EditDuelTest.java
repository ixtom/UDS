package com.ixtom.uds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;

import com.ixtom.uds.model.Duel;
import com.ixtom.uds.model.UserAuth;

/**
 * Story 21's test table in the requirements doc is empty. These tests are derived from the
 * story text: a competitor can upload, update, or replace their own side of the duel.
 */
class EditDuelTest extends IntegrationTest {

    private UserAuth leftOwner;
    private UserAuth rightOwner;
    private Duel duel;

    private void duelBetweenTwoUsers() {
        leftOwner = createUser("benny");
        rightOwner = createUser("keith");
        duel = createDuel(createDuelist(leftOwner, "Blaze"), createDuelist(rightOwner, "Frost"));
        flushAndClear();
    }

    private Duel reload() {
        flushAndClear();
        return duelRepo.findById(duel.getId()).orElseThrow();
    }

    // Story 21 (no table row): a competitor uploads their video and only their side changes
    @Test
    void competitorUpdatesOnlyOwnSide() throws Exception {
        duelBetweenTwoUsers();

        mvc.perform(post("/duel/" + duel.getId() + "/edit")
                .param("video", "https://www.youtube.com/watch?v=9bZkp7q19f0")
                .param("banner", "")
                .param("title", "")
                .param("description", "")
                .with(as(rightOwner))
                .with(csrf()))
            .andExpect(redirectedUrl("/duelpage/" + duel.getId()));

        Duel saved = reload();
        assertThat(saved.getVideoid2()).isEqualTo("9bZkp7q19f0");
        assertThat(saved.getVideoid1()).isEmpty();
    }

    // Story 21 (no table row): a competitor replaces a video they already uploaded
    @Test
    void competitorReplacesOwnVideo() throws Exception {
        duelBetweenTwoUsers();
        Duel existing = duelRepo.findById(duel.getId()).orElseThrow();
        existing.setVideoid1("dQw4w9WgXcQ");
        duelRepo.save(existing);
        flushAndClear();

        mvc.perform(post("/duel/" + duel.getId() + "/edit")
                .param("video", "https://youtu.be/9bZkp7q19f0")
                .param("banner", "")
                .param("title", "")
                .param("description", "")
                .with(as(leftOwner))
                .with(csrf()))
            .andExpect(redirectedUrl("/duelpage/" + duel.getId()));

        assertThat(reload().getVideoid1()).isEqualTo("9bZkp7q19f0");
    }

    // Story 21 (no table row): an invalid YouTube link is rejected and nothing is saved
    @Test
    void invalidVideoLinkIsRejected() throws Exception {
        duelBetweenTwoUsers();

        mvc.perform(post("/duel/" + duel.getId() + "/edit")
                .param("video", "https://example.com/video")
                .param("banner", "")
                .param("title", "")
                .param("description", "")
                .with(as(leftOwner))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("duel/edit"))
            .andExpect(model().attributeHasFieldErrors("editDuelRequest", "video"));

        assertThat(reload().getVideoid1()).isEmpty();
    }

    // Story 21 (no table row): a user who is not competing in the duel cannot edit it
    @Test
    void nonCompetitorCannotEdit() throws Exception {
        duelBetweenTwoUsers();
        UserAuth outsider = createUser("outsider");

        mvc.perform(post("/duel/" + duel.getId() + "/edit")
                .param("video", "https://youtu.be/9bZkp7q19f0")
                .param("banner", "")
                .param("title", "")
                .param("description", "")
                .with(as(outsider))
                .with(csrf()))
            .andExpect(status().isForbidden());

        Duel saved = reload();
        assertThat(saved.getVideoid1()).isEmpty();
        assertThat(saved.getVideoid2()).isEmpty();
    }

    // Story 21 (no table row): a logged-out visitor cannot edit a duel
    @Test
    void loggedOutVisitorCannotEdit() throws Exception {
        duelBetweenTwoUsers();

        mvc.perform(post("/duel/" + duel.getId() + "/edit")
                .param("video", "https://youtu.be/9bZkp7q19f0")
                .param("banner", "")
                .param("title", "")
                .param("description", "")
                .with(csrf()))
            .andExpect(status().isUnauthorized());

        assertThat(reload().getVideoid1()).isEmpty();
    }
}
