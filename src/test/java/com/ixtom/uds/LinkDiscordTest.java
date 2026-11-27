package com.ixtom.uds;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.ixtom.uds.dto.DiscordUserDataResponse;
import com.ixtom.uds.model.AccountLink;
import com.ixtom.uds.model.UserAuth;
import com.ixtom.uds.repository.AccountLinkRepository;
import com.ixtom.uds.service.DiscordService;
import com.ixtom.uds.service.OAuthService.Result;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

class LinkDiscordTest extends IntegrationTest {

    @MockitoSpyBean
    private DiscordService discordService;

    @Autowired
    private AccountLinkRepository accountLinkRepo;

    @MockitoSpyBean
    private OAuth2AuthorizedClientRepository authorizedClientRepository;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    private static final DiscordUserDataResponse one = new DiscordUserDataResponse(1, "a@b.c", "one", null, true);
    private static final DiscordUserDataResponse two = new DiscordUserDataResponse(2, "b@b.c", "two", null, false);
    private static final DiscordUserDataResponse three = new DiscordUserDataResponse(3, "c@b.c", "three", null, true);
    private static final DiscordUserDataResponse four = new DiscordUserDataResponse(4, "d@b.c", "four", null, true);

    /**
     * Mocks the Discord account returned by the OAuth service.
     */
    private void discordApprovesAs(String discordUserId) {
        when(discordService.getExternalId())
            .thenReturn(discordUserId);
    }

    private OAuth2AuthorizedClient createOAuthClient(String provider, String principleName) {
        ClientRegistration registration =
            clientRegistrationRepository.findByRegistrationId(provider);

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "test-access-token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            Set.of("openid", "profile", "email")
        );

        OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(
            "test-refresh-token",
            Instant.now()
        );

        return new OAuth2AuthorizedClient(
            registration,
            principleName,
            accessToken,
            refreshToken
        );
    }

    private void discordApprovesAs(DiscordUserDataResponse data) {
        doReturn(data).when(discordService).getUserData();

        doReturn(createOAuthClient("discord", "asdf")).when(authorizedClientRepository)
            .loadAuthorizedClient(
                eq("discord"),
                any(Authentication.class),
                any(HttpServletRequest.class)
            );
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

    @Test
    void discordLoginSuccess() throws Exception {
        UserAuth user = createUser("user1");
        Cookie session = login(user.getEmail(), PASSWORD);

        discordApprovesAs(one);
        linkDiscord(session);

        discordApprovesAs(one);
        mvc.perform(get("/login/discord"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));
    }

    @Test
    void discordLoginNoLinkedAccountPromptsRegister() throws Exception {
        discordApprovesAs(one);

        mvc.perform(get("/login/discord"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/register/discord"));
    }

    @Test
    void discordRegisterAutofillsForm() throws Exception {
        discordApprovesAs(one);

        mvc.perform(get("/register/discord"))
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().string(containsString(one.email())))
            .andExpect(content().string(containsString(one.username())));
    }

    @Test
    void discordRegisterCreatesAccount() throws Exception {
        discordApprovesAs(one);

        mvc.perform(post("/register/discord")
            .param("email", one.email())
            .param("username", one.username())
            .param("cf-turnstile-response", VALID_TURNSTILE_TOKEN)
            .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));
    }
}
