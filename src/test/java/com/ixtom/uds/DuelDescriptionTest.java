package com.ixtom.uds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;

import com.ixtom.uds.model.Duel;
import com.ixtom.uds.model.UserAuth;

class DuelDescriptionTest extends IntegrationTest {

    // Story 7, Success row: write a paragraph under 2000 characters and save -> it shows on the duel page
    @Test
    void shortDescriptionIsSavedAndShown() throws Exception {
        UserAuth owner = createUser("justin");
        Duel duel = createDuel(createDuelist(owner, "Blaze"), createDuelist(createUser("keith"), "Frost"));

        mvc.perform(post("/duel/" + duel.getId() + "/edit")
                .param("video", "")
                .param("banner", "")
                .param("title", "")
                .param("description", "Blaze returns to settle an old score.")
                .with(as(owner))
                .with(csrf()))
            .andExpect(redirectedUrl("/duelpage/" + duel.getId()));

        flushAndClear();
        mvc.perform(get("/duelpage/" + duel.getId()))
            .andExpect(content().string(containsString("Blaze returns to settle an old score.")));
    }

    // Story 7, Failure row: write a paragraph over 5000 characters and save -> error and nothing is saved
    @Test
    void descriptionOver5000CharactersIsRejected() throws Exception {
        UserAuth owner = createUser("justin");
        Duel duel = createDuel(createDuelist(owner, "Blaze"), createDuelist(createUser("keith"), "Frost"));
        duel.setDescription("Original description");
        duelRepo.save(duel);
        flushAndClear();

        mvc.perform(post("/duel/" + duel.getId() + "/edit")
                .param("video", "")
                .param("banner", "")
                .param("title", "")
                .param("description", "a".repeat(5001))
                .with(as(owner))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("duel/edit"))
            .andExpect(model().attributeHasFieldErrors("editDuelRequest", "description"));

        flushAndClear();
        assertThat(duelRepo.findById(duel.getId()).orElseThrow().getDescription()).isEqualTo("Original description");
    }
}
