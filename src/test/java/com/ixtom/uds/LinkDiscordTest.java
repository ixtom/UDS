package com.ixtom.uds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.ixtom.uds.dto.DiscordUserDataResponse;
import com.ixtom.uds.model.AccountLink;
import com.ixtom.uds.model.UserAuth;
import com.ixtom.uds.repository.AccountLinkRepository;
import com.ixtom.uds.service.DiscordService;
import com.ixtom.uds.service.OAuthService.Result;

import jakarta.servlet.http.Cookie;

class LinkDiscordTest extends IntegrationTest {

    @MockitoSpyBean
    private DiscordService discordService;

    @Autowired
    private AccountLinkRepository accountLinkRepo;

    private static final DiscordUserDataResponse one = new DiscordUserDataResponse(1, "a@b.c", "asdf", null, true);
    private static final DiscordUserDataResponse two = new DiscordUserDataResponse(2, "b@b.c", "fdsa", null, false);
    private static final DiscordUserDataResponse three = new DiscordUserDataResponse(3, "c@b.c", "fasdfa", null, true);
    private static final DiscordUserDataResponse four = new DiscordUserDataResponse(4, "d@b.c", "dsaf", null, true);

    /**
     * Mocks the Discord account returned by the OAuth service.
     */
    private void discordApprovesAs(String discordUserId) {
        when(discordService.getExternalId())
            .thenReturn(discordUserId);
    }

    private void discordApprovesAs(DiscordUserDataResponse data) {
        doReturn(data).when(discordService).getUserData();
    }

    /**
     * Performs the account-linking request.
     */
    private void linkDiscord(Cookie session) throws Exception {
        mvc.perform(get("/link/discord").cookie(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));
    }

    // Story 9, Success:
    // Link a Discord account successfully.
    @Test
    void linkingSucceeds() throws Exception {
        UserAuth user = createUser("benny");
        Cookie session = login(user.getEmail(), PASSWORD);
        discordApprovesAs(one);

        linkDiscord(session);

        AccountLink link = accountLinkRepo
            .findByProviderAndUserId("discord", user.getId())
            .orElseThrow();

        assertThat(link.getId().getExternalId())
            .isEqualTo("1");
    }

    // Story 9, Failure:
    // Reject a Discord account already linked to another user.
    @Test
    void discordAccountLinkedToAnotherUserIsRejected() throws Exception {
        UserAuth other = createUser("first_owner");

        accountLinkRepo.save(
            new AccountLink("discord", "2", other)
        );

        UserAuth user = createUser("benny");

        Cookie session = login(user.getEmail(), PASSWORD);
        discordApprovesAs(two);

        mvc.perform(get("/link/discord").cookie(session))
            .andExpect(status().isBadRequest())
            .andExpect(status().reason(Result.OAUTH_ALREADY_LINKED.message));

        assertThat(accountLinkRepo.findByProviderAndUserId("discord", user.getId()))
            .isEmpty();

        verify(discordService).revokeToken();
    }

    // Story 9, Failure:
    // Reject linking a second Discord account to the same user.
    @Test
    void accountAlreadyLinkedIsRejected() throws Exception {
        UserAuth user = createUser("benny");

        accountLinkRepo.save(
            new AccountLink("discord", "3", user)
        );

        Cookie session = login(user.getEmail(), PASSWORD);
        discordApprovesAs(four);

        mvc.perform(get("/link/discord").cookie(session))
            .andExpect(status().isBadRequest())
            .andExpect(status().reason(Result.USER_ALREADY_LINKED.message));

        assertThat(accountLinkRepo.findByUserId(user.getId()))
            .extracting(link -> link.getId().getExternalId())
            .containsExactly("3");
    }
}
