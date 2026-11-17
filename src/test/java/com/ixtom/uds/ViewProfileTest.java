package com.ixtom.uds;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;

import com.ixtom.uds.model.UserAuth;
import com.ixtom.uds.model.UserProfile;

class ViewProfileTest extends IntegrationTest {

    // Story 10, Success row: while logged out, open /profile/{username} for an existing user -> profile displayed without requiring login
    @Test
    void profileIsPublicWhenLoggedOut() throws Exception {
        UserAuth user = createUser("jayden");
        UserProfile profile = user.getProfile();
        profile.setBio("I animate stick fights.");
        userProfileRepo.save(profile);
        flushAndClear();

        mvc.perform(get("/profile/jayden"))
            .andExpect(status().isOk())
            .andExpect(view().name("profile/view"))
            .andExpect(content().string(containsString("jayden")))
            .andExpect(content().string(containsString("I animate stick fights.")));
    }

    // Story 10, Success row: open a profile with no biography, duelists, or social links -> empty-state messages displayed
    @Test
    void emptyProfileShowsEmptyStateMessages() throws Exception {
        createUser("blank_user");
        flushAndClear();

        mvc.perform(get("/profile/blank_user"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("No bio yet")))
            .andExpect(content().string(containsString("No duelists yet")))
            .andExpect(content().string(containsString("No social links yet")));
    }

    // Story 10, Failure row: open a profile URL with a nonexistent username -> 404 not-found response
    @Test
    void unknownUsernameReturnsNotFound() throws Exception {
        mvc.perform(get("/profile/no_such_user"))
            .andExpect(status().isNotFound());
    }
}
