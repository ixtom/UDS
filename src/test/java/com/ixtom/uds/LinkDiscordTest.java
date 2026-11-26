package com.ixtom.uds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.ixtom.uds.model.AccountLink;
import com.ixtom.uds.model.UserAuth;
import com.ixtom.uds.repository.AccountLinkRepository;
import com.ixtom.uds.service.DiscordService;

import jakarta.servlet.http.Cookie;

/**
 * Discord itself is replaced with mocks: the test pretends Discord already approved the
 * user and reports the given Discord user ID. No request leaves the machine.
 */
class LinkDiscordTest extends IntegrationTest {

    @MockitoBean
    private DiscordService discordService;

    @MockitoBean
    private OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    private ClientRegistrationRepository clientRegistrations;

    @Autowired
    private AccountLinkRepository accountLinkRepo;

    /** Signs in and presses "Link Discord", which records the link attempt in the session. */
    private Cookie startLinking(UserAuth user) throws Exception {
        Cookie session = login(user.getEmail(), PASSWORD);
        mvc.perform(get("/link/discord").cookie(session))
            .andExpect(redirectedUrl("/oauth2/authorization/discord"));
        return session;
    }

    private void discordApprovesAs(String discordUserId) {
        OAuth2AuthorizedClient client = new OAuth2AuthorizedClient(
            clientRegistrations.findByRegistrationId("discord"),
            "test-principal",
            new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "fake-token",
                Instant.now(), Instant.now().plusSeconds(3600)));
        when(authorizedClientService.<OAuth2AuthorizedClient>loadAuthorizedClient(eq("discord"), anyString()))
            .thenReturn(client);
        when(discordService.getOAuthUserInfo().externalId()).thenReturn(discordUserId);
    }

    // Story 9, Success row: press the link discord button & follow through with discord authentication -> Discord gets linked
    @Test
    void linkingSucceeds() throws Exception {
        UserAuth user = createUser("benny");
        Cookie session = startLinking(user);
        discordApprovesAs("111111111111111111");

        mvc.perform(get("/login/oauth2/code/discord").cookie(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));

        AccountLink link = accountLinkRepo.findByProviderAndUserId("discord", user.getId()).orElseThrow();
        assertThat(link.getId().getExternalId()).isEqualTo("111111111111111111");
    }

    // Story 9, Failure row: linking a discord account that is already linked to another user's account -> error message displayed
    @Test
    void discordAccountLinkedToAnotherUserIsRejected() throws Exception {
        UserAuth other = createUser("first_owner");
        accountLinkRepo.save(new AccountLink("discord", "222222222222222222", other));
        UserAuth user = createUser("benny");
        Cookie session = startLinking(user);
        discordApprovesAs("222222222222222222");

        mvc.perform(get("/login/oauth2/code/discord").cookie(session))
            .andExpect(status().isBadRequest())
            .andExpect(model().attribute("error", "This account is already linked to another user"));

        assertThat(accountLinkRepo.findByProviderAndUserId("discord", user.getId())).isEmpty();
        verify(discordService).revokeToken();
    }

    // Story 9, Failure row: link discord to an account that already has discord linked -> error message displayed
    @Test
    void accountAlreadyLinkedIsRejected() throws Exception {
        UserAuth user = createUser("benny");
        accountLinkRepo.save(new AccountLink("discord", "333333333333333333", user));
        Cookie session = startLinking(user);
        discordApprovesAs("444444444444444444");

        mvc.perform(get("/login/oauth2/code/discord").cookie(session))
            .andExpect(status().isBadRequest())
            .andExpect(model().attribute("error", "Your account is already linked to this service"));

        assertThat(accountLinkRepo.findByUserId(user.getId()))
            .extracting(link -> link.getId().getExternalId())
            .containsExactly("333333333333333333");
    }
}
