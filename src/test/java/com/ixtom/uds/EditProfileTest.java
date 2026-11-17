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

import com.ixtom.uds.model.UserAuth;
import com.ixtom.uds.model.UserProfile;

class EditProfileTest extends IntegrationTest {

    // Story 11, Success row: sign in, open Edit Profile, enter a biography within 2,000 characters, and save -> saved and displayed
    @Test
    void biographyIsSavedAndDisplayed() throws Exception {
        UserAuth user = createUser("jayden");

        mvc.perform(get("/account/profile/edit").with(as(user)))
            .andExpect(status().isOk())
            .andExpect(view().name("profile/edit"));

        mvc.perform(post("/account/profile/edit")
                .param("bio", "Stick animator since 2009.")
                .param("youtubeUrl", "")
                .param("instagramUrl", "")
                .with(as(user))
                .with(csrf()))
            .andExpect(redirectedUrl("/profile/jayden"));

        flushAndClear();
        mvc.perform(get("/profile/jayden"))
            .andExpect(content().string(containsString("Stick animator since 2009.")));
    }

    // Story 11, Success row: sign in as one user and view another user's profile -> no Edit Profile link; editing only touches the signed-in user's profile
    @Test
    void userCanOnlyEditOwnProfile() throws Exception {
        UserAuth jayden = createUser("jayden");
        UserAuth other = createUser("other_user");

        mvc.perform(get("/profile/other_user").with(as(jayden)))
            .andExpect(status().isOk())
            .andExpect(model().attribute("isOwner", false));

        mvc.perform(post("/account/profile/edit")
                .param("bio", "Jayden's new bio")
                .param("youtubeUrl", "")
                .param("instagramUrl", "")
                .with(as(jayden))
                .with(csrf()))
            .andExpect(redirectedUrl("/profile/jayden"));

        flushAndClear();
        assertThat(userProfileRepo.findById(jayden.getId()).orElseThrow().getBio()).isEqualTo("Jayden's new bio");
        assertThat(userProfileRepo.findById(other.getId()).orElseThrow().getBio()).isNull();
    }

    // Story 11, Failure row: submit a biography longer than 2,000 characters -> validation error, existing biography unchanged
    @Test
    void biographyOver2000CharactersIsRejected() throws Exception {
        UserAuth user = createUser("jayden");
        UserProfile profile = user.getProfile();
        profile.setBio("Existing bio");
        userProfileRepo.save(profile);
        flushAndClear();

        mvc.perform(post("/account/profile/edit")
                .param("bio", "b".repeat(2001))
                .param("youtubeUrl", "")
                .param("instagramUrl", "")
                .with(as(user))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("profile/edit"))
            .andExpect(model().attributeHasFieldErrors("editProfile", "bio"))
            .andExpect(content().string(containsString("Bio must be 2000 characters or fewer.")));

        flushAndClear();
        assertThat(userProfileRepo.findById(user.getId()).orElseThrow().getBio()).isEqualTo("Existing bio");
    }

    // Story 11, Failure row: while logged out, open /account/profile/edit -> redirected to the login page
    @Test
    void loggedOutUserIsSentToLogin() throws Exception {
        mvc.perform(get("/account/profile/edit"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }
}
