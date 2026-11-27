package com.ixtom.uds.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

import com.ixtom.uds.dto.OAuthUserInfo;
import com.ixtom.uds.model.AccountLink;
import com.ixtom.uds.model.UserAuth;
import com.ixtom.uds.model.UserAuthPrincipal;
import com.ixtom.uds.repository.AccountLinkRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class OAuthService {
    @Autowired
    private OAuth2AuthorizedClientRepository authorizedClientRepository;

    @Autowired
    private AccountLinkRepository accountLinkRepo;

    @Autowired
    private DiscordService discordService;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    private SecurityContextRepository securityContextRepository;

    @Autowired
    private SessionAuthenticationStrategy sessionAuthenticationStrategy;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    public void deauthorize(String provider) {
        switch (provider) {
        	case "discord": discordService.revokeToken();
        	default: {};
        };

        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof UserAuthPrincipal principal) {
            authorizedClientService.removeAuthorizedClient(provider, principal.getUsername());
        }
    }

    public void login(AccountLink link, HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthorizedClient client = authorizedClientRepository.loadAuthorizedClient(link.getId().getProvider(), auth, request);

        if (client == null) {
            throw new IllegalStateException("OAuth authorized client not found");
        }

        authorizedClientRepository.removeAuthorizedClient(link.getId().getProvider(), auth, request, response);

        UserAuthPrincipal principal = new UserAuthPrincipal(link.getUser());
        auth = UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());

        setAuthToken(auth, request, response);

        OAuth2AuthorizedClient newClient = new OAuth2AuthorizedClient(
            client.getClientRegistration(),
            auth.getName(),
            client.getAccessToken(),
            client.getRefreshToken()
        );

        authorizedClientRepository.saveAuthorizedClient(newClient, auth, request, response);
    }

    public String getExternalId(String provider) {
       return switch (provider) {
           case "discord" -> discordService.getExternalId();
           // case "provider" -> providerService.getOAuthUserInfo();
           default -> null; // internal error
       };
    }

    public OAuthUserInfo getOAuthUserInfo(String provider) {
       return switch (provider) {
           case "discord" -> discordService.getOAuthUserInfo();
           // case "provider" -> providerService.getOAuthUserInfo();
           default -> null; // internal error
       };
    }

    public boolean isProvider(String provider) {
        return clientRegistrationRepository.findByRegistrationId(provider) != null;
    }

    public Result link(String provider, String externalId, UserAuth user) {
        if (!isProvider(provider)) {
            return Result.UNKNOWN_OAUTH_PROVIDER;
        } else if (accountLinkRepo.existsByProviderAndUserId(provider, user.getId())) {
            return Result.USER_ALREADY_LINKED;
        } else if (accountLinkRepo.existsByProviderAndExternalId(provider, externalId)) {
            return Result.OAUTH_ALREADY_LINKED;
        }

        accountLinkRepo.save(new AccountLink(provider, externalId, user));
        return Result.LINK_SUCCESS;
    }

    public void unlink(String provider, Long userId) {
        isProvider(provider); // TODO

        AccountLink link = accountLinkRepo.findByProviderAndUserId(provider, userId)
            .orElseThrow(() -> new IllegalArgumentException("Account link not found"));

        accountLinkRepo.delete(link);
    }

    // TODO: make an auth service
    private void setAuthToken(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
	    sessionAuthenticationStrategy.onAuthentication(authentication, request, response);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(context, request, response);
	}

	public static enum Result {
        USER_ALREADY_LINKED(false, "Your account is already linked to this service"),
        OAUTH_ALREADY_LINKED(false, "This account is already linked to another user"),
        LINK_SUCCESS(true, "Linked account successfully"),

        UNKNOWN_OAUTH_PROVIDER(false, "Unknown OAuth provider"),

        LOGIN_SUCCESS(true, "Logged in"),

        NOT_IMPLEMENTED(false, "Unimplemented"),
        ;

        public final boolean success;
        public final String message;

        Result(boolean success, String message) {
				this.success = success;
				this.message = message;
        }
    }
}
