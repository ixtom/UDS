package com.ixtom.uds.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;

import com.ixtom.uds.model.AccountLink;
import com.ixtom.uds.model.UserAuth;
import com.ixtom.uds.model.UserAuthPrincipal;
import com.ixtom.uds.repository.AccountLinkRepository;
import com.ixtom.uds.repository.UserAuthRepository;

@Service
public class OAuthHandlerService {

    @Autowired
    private UserAuthRepository userAuthRepo;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    private AccountLinkRepository linkedAccountRepo;

    @Autowired
    private DiscordService discordService;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    public boolean isProvider(String provider) {
        return clientRegistrationRepository.findByRegistrationId(provider) != null;
    }

    public ActionResult onOAuthLogin(String provider, OAuthAction action) {
        return switch (action) {
            case LINK -> linkCurrentUser(provider);
            case LOGIN -> ActionResult.NOT_IMPLEMENTED;
            case SIGNUP -> ActionResult.NOT_IMPLEMENTED;
        };
    }

    public void deauthorize(String provider) {
        switch (provider) {
        	case "discord": discordService.revokeToken();
        	default: {};
        };
    }

    public ActionResult linkCurrentUser(String provider) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(provider, auth.getName());

        if (auth == null || !(auth.getPrincipal() instanceof UserAuthPrincipal principle)) {
            return ActionResult.USER_UNAUTHORIZED;
        } else if (client == null) {
            return ActionResult.OAUTH_AUTHORIZATION_MISSING;
        } else if (linkedAccountRepo.findByProviderAndUserId(provider, principle.getId()).isPresent()) {
            return ActionResult.USER_ALREADY_LINKED;
        }

        String externalId = switch (provider) {
        	case "discord" -> discordService.getExternalId();
            // case "provider" -> providerService.getExternalId();
        	default -> null; // internal error
        };

        if (linkedAccountRepo.findByProviderAndExternalId(provider, externalId).isPresent()) {
            return ActionResult.OAUTH_ALREADY_LINKED;
        }

        UserAuth user = userAuthRepo.getReferenceById(principle.getId());
        linkedAccountRepo.save(new AccountLink(provider, externalId, user));

        return ActionResult.LINK_SUCCESS;
    }

    public static enum OAuthAction {
        LINK,
        LOGIN,
        SIGNUP,
    }

    public static enum ActionResult {
        USER_UNAUTHORIZED(false, "Please log in first"),
        OAUTH_AUTHORIZATION_MISSING(false, "No OAuth authorization found"),
        USER_ALREADY_LINKED(false, "Your account is already linked to this service"),
        OAUTH_ALREADY_LINKED(false, "This account is already linked to another user"),
        LINK_SUCCESS(true, "Linked account successfully"),

        NOT_IMPLEMENTED(false, "Unimplemented"),
        ;

        public final boolean success;
        public final String message;

        ActionResult(boolean success, String message) {
			this.success = success;
			this.message = message;
        }
    }
}
