package com.ixtom.uds;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;

import com.ixtom.uds.model.Duel;

class ViewDuelTest extends IntegrationTest {

    private Duel duelWithVideos() {
        Duel duel = createDuel(
            createDuelist(createUser("keith"), "Blaze"),
            createDuelist(createUser("justin"), "Frost"));
        duel.setVideoid1("dQw4w9WgXcQ");
        duel.setVideoid2("9bZkp7q19f0");
        duel.setDescription("Two rivals settle it on the rooftop.");
        return duelRepo.save(duel);
    }

    // Story 6, Success row: click a duel in the list -> duel page shows the animation
    @Test
    void duelPageShowsAnimation() throws Exception {
        Duel duel = duelWithVideos();
        flushAndClear();

        mvc.perform(get("/"))
            .andExpect(content().string(containsString("/duelpage/" + duel.getId())));

        mvc.perform(get("/duelpage/" + duel.getId()))
            .andExpect(status().isOk())
            .andExpect(view().name("duel/view"))
            .andExpect(content().string(containsString("Blaze vs Frost")))
            .andExpect(content().string(containsString("Two rivals settle it on the rooftop.")))
            .andExpect(content().string(containsString("<iframe")));
    }

    // Story 6, Failure row: open a duel that does not exist -> not-found page, no crash
    @Test
    void missingDuelReturnsNotFound() throws Exception {
        mvc.perform(get("/duelpage/999999"))
            .andExpect(status().isNotFound());
    }

    // Story 6, Success row: click a video in the duel page -> the video plays in the web page
    @Test
    void duelPageEmbedsBothYouTubeVideos() throws Exception {
        Duel duel = duelWithVideos();
        flushAndClear();

        mvc.perform(get("/duelpage/" + duel.getId()))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("https://www.youtube-nocookie.com/embed/dQw4w9WgXcQ")))
            .andExpect(content().string(containsString("https://www.youtube-nocookie.com/embed/9bZkp7q19f0")));
    }
}
