package com.ixtom.uds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.ixtom.uds.model.UserAuth;
import com.ixtom.uds.model.UserProfile;

class SocialLinksTest extends IntegrationTest {

    private static final String YOUTUBE = "https://www.youtube.com/@uds_channel";
    private static final String INSTAGRAM = "https://www.instagram.com/uds_insta";

    private UserAuth userWithLinks() {
        UserAuth user = createUser("jayden");
        UserProfile profile = user.getProfile();
        profile.setYoutubeUrl(YOUTUBE);
        profile.setInstagramUrl(INSTAGRAM);
        userProfileRepo.save(profile);
        flushAndClear();
        return user;
    }

    // Story 13, Success row: save full HTTPS links to a YouTube /@handle and an Instagram profile -> both links appear on the public profile
    @Test
    void validLinksAppearOnProfile() throws Exception {
        UserAuth user = createUser("jayden");

        mvc.perform(post("/account/profile/edit")
                .param("bio", "")
                .param("youtubeUrl", YOUTUBE)
                .param("instagramUrl", INSTAGRAM)
                .with(as(user))
                .with(csrf()))
            .andExpect(redirectedUrl("/profile/jayden"));

        flushAndClear();
        mvc.perform(get("/profile/jayden"))
            .andExpect(content().string(containsString("href=\"" + YOUTUBE + "\"")))
            .andExpect(content().string(containsString("href=\"" + INSTAGRAM + "\"")))
            .andExpect(content().string(not(containsString("No social links yet"))));
    }

    // Story 13, Success row: clear both social-link fields and save -> links removed and "No social links yet" displayed
    @Test
    void clearingLinksRemovesThem() throws Exception {
        UserAuth user = userWithLinks();

        mvc.perform(post("/account/profile/edit")
                .param("bio", "")
                .param("youtubeUrl", "")
                .param("instagramUrl", "")
                .with(as(user))
                .with(csrf()))
            .andExpect(redirectedUrl("/profile/jayden"));

        flushAndClear();
        mvc.perform(get("/profile/jayden"))
            .andExpect(content().string(containsString("No social links yet")))
            .andExpect(content().string(not(containsString(YOUTUBE))));
    }

    // Story 13, Failure row: enter https://example.com/profile in either social-link field and save -> validation error, link not saved
    @ParameterizedTest(name = "invalid link in {0}")
    @ValueSource(strings = {"youtubeUrl", "instagramUrl"})
    void nonSocialLinkIsRejected(String field) throws Exception {
        UserAuth user = userWithLinks();

        mvc.perform(post("/account/profile/edit")
                .param("bio", "")
                .param("youtubeUrl", YOUTUBE)
                .param("instagramUrl", INSTAGRAM)
                .param(field, "https://example.com/profile")
                .with(as(user))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("profile/edit"))
            .andExpect(model().attributeHasFieldErrors("editProfile", field));

        flushAndClear();
        UserProfile saved = userProfileRepo.findById(user.getId()).orElseThrow();
        assertThat(saved.getYoutubeUrl()).isEqualTo(YOUTUBE);
        assertThat(saved.getInstagramUrl()).isEqualTo(INSTAGRAM);
    }

    // Story 13, Failure row: submit a social link longer than 2,048 characters -> validation error, change not saved
    @Test
    void linkOver2048CharactersIsRejected() throws Exception {
        UserAuth user = userWithLinks();
        String tooLong = "https://www.youtube.com/@" + "a".repeat(2048);

        mvc.perform(post("/account/profile/edit")
                .param("bio", "")
                .param("youtubeUrl", tooLong)
                .param("instagramUrl", INSTAGRAM)
                .with(as(user))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors("editProfile", "youtubeUrl"))
            .andExpect(content().string(containsString("Link must be 2048 characters or fewer.")));

        flushAndClear();
        assertThat(userProfileRepo.findById(user.getId()).orElseThrow().getYoutubeUrl()).isEqualTo(YOUTUBE);
    }
}
