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

import java.util.List;

import org.junit.jupiter.api.Test;

import com.ixtom.uds.model.Duelist;
import com.ixtom.uds.model.UserAuth;

/**
 * Story 14's test table in the requirements doc is empty. These tests are derived from the
 * story's postconditions (a duelist is created / modified and owned by the logged-in user).
 */
class DuelistTest extends IntegrationTest {

    // Story 14 (no table row; postcondition): a new duelist is created and owned by the user
    @Test
    void createDuelistSavesItForOwner() throws Exception {
        UserAuth user = createUser("benny");

        mvc.perform(post("/duelist/create")
                .param("name", "Shadow Blade")
                .param("title", "The Silent")
                .param("description", "Trained in the mountains.")
                .param("demoVideo", "https://youtu.be/dQw4w9WgXcQ")
                .with(as(user))
                .with(csrf()))
            .andExpect(status().is3xxRedirection());

        flushAndClear();
        List<Duelist> owned = duelistRepo.findByOwnerIdOrderByNameAsc(user.getId());
        assertThat(owned).extracting(Duelist::getName).containsExactly("Shadow Blade");
        assertThat(owned.get(0).getDemo()).isEqualTo("dQw4w9WgXcQ");

        mvc.perform(get("/duelist/" + owned.get(0).getId()))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Shadow Blade")))
            .andExpect(content().string(containsString("Trained in the mountains.")));
    }

    // Story 14 (no table row; precondition user is logged in): logged-out create is refused and nothing is saved
    @Test
    void loggedOutCreateIsRefused() throws Exception {
        long before = duelistRepo.count();

        mvc.perform(post("/duelist/create")
                .param("name", "Shadow Blade")
                .param("title", "")
                .param("description", "")
                .param("demoVideo", "")
                .with(csrf()))
            .andExpect(status().isUnauthorized());

        assertThat(duelistRepo.count()).isEqualTo(before);
    }

    // Story 14 (no table row): a duelist with a blank name is rejected and not saved
    @Test
    void blankNameIsRejected() throws Exception {
        UserAuth user = createUser("benny");

        mvc.perform(post("/duelist/create")
                .param("name", "")
                .param("title", "")
                .param("description", "")
                .param("demoVideo", "")
                .with(as(user))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("duelist/create"))
            .andExpect(model().attributeHasFieldErrors("createDuelistRequest", "name"));

        assertThat(duelistRepo.findByOwnerIdOrderByNameAsc(user.getId())).isEmpty();
    }

    // Story 14 (no table row; postcondition): a preexisting duelist owned by the user is modified
    @Test
    void ownerCanEditDuelist() throws Exception {
        UserAuth user = createUser("benny");
        Duelist duelist = createDuelist(user, "Shadow Blade");

        mvc.perform(post("/duelist/" + duelist.getId() + "/edit")
                .param("name", "Shadow Blade II")
                .param("title", "Reborn")
                .param("description", "Back for revenge.")
                .param("demoVideo", "")
                .with(as(user))
                .with(csrf()))
            .andExpect(redirectedUrl("/duelist/" + duelist.getId()));

        flushAndClear();
        Duelist saved = duelistRepo.findById(duelist.getId()).orElseThrow();
        assertThat(saved.getName()).isEqualTo("Shadow Blade II");
        assertThat(saved.getDescription()).isEqualTo("Back for revenge.");
    }

    // Story 14 (no table row): a user cannot edit someone else's duelist
    @Test
    void nonOwnerCannotEditDuelist() throws Exception {
        Duelist duelist = createDuelist(createUser("benny"), "Shadow Blade");
        UserAuth intruder = createUser("intruder");
        flushAndClear();

        mvc.perform(post("/duelist/" + duelist.getId() + "/edit")
                .param("name", "Hijacked")
                .param("title", "")
                .param("description", "")
                .param("demoVideo", "")
                .with(as(intruder))
                .with(csrf()))
            .andExpect(status().isForbidden());

        flushAndClear();
        assertThat(duelistRepo.findById(duelist.getId()).orElseThrow().getName()).isEqualTo("Shadow Blade");
    }
}
