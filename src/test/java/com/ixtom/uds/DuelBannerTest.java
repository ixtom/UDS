package com.ixtom.uds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import com.ixtom.uds.model.Duel;
import com.ixtom.uds.model.UserAuth;

/**
 * The upload itself (file picking, cropping, PNG/size checks) happens in the browser through the
 * Cloudinary widget, so only the server side of Story 15 can be tested here: saving the banner
 * link from the duel-edit form and showing it on the duel page.
 */
class DuelBannerTest extends IntegrationTest {

    private static final String BANNER = "https://res.cloudinary.com/demo/image/upload/c_crop,ar_4:1/banner.png";

    // Story 15, Success row (server half): submit the edit form with the uploaded banner link -> duel page displays the banner
    @Test
    void competitorSavesBannerAndItIsShown() throws Exception {
        UserAuth owner = createUser("justin");
        Duel duel = createDuel(createDuelist(createUser("keith"), "Frost"), createDuelist(owner, "Blaze"));

        mvc.perform(post("/duel/" + duel.getId() + "/edit")
                .param("video", "")
                .param("banner", BANNER)
                .param("title", "")
                .param("description", "")
                .with(as(owner))
                .with(csrf()))
            .andExpect(redirectedUrl("/duelpage/" + duel.getId()));

        flushAndClear();
        mvc.perform(get("/duelpage/" + duel.getId()))
            .andExpect(content().string(containsString("src=\"" + BANNER + "\"")));
    }

    // Story 15 (acceptance criterion): users who do not own either duelist cannot edit the duel's banner
    @Test
    void nonCompetitorCannotChangeBanner() throws Exception {
        Duel duel = createDuel(createDuelist(createUser("keith"), "Frost"), createDuelist(createUser("justin"), "Blaze"));
        duel.setDuelBanner(BANNER);
        duelRepo.save(duel);
        UserAuth outsider = createUser("outsider");
        flushAndClear();

        mvc.perform(post("/duel/" + duel.getId() + "/edit")
                .param("video", "")
                .param("banner", "https://res.cloudinary.com/demo/image/upload/other.png")
                .param("title", "")
                .param("description", "")
                .with(as(outsider))
                .with(csrf()))
            .andExpect(status().isForbidden());

        flushAndClear();
        assertThat(duelRepo.findById(duel.getId()).orElseThrow().getDuelBanner()).isEqualTo(BANNER);
    }
}
