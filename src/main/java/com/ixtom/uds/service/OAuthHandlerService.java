package com.ixtom.uds.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
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


    public ActionResult onOAuthLogin(String provider, OAuthAction action) {
        return switch (action) {
            case LINK -> linkCurrentUser(provider);
            case LOGIN -> ActionResult.NOT_IMPLEMENTED;
            case SIGNUP -> ActionResult.NOT_IMPLEMENTED;
        };
    }

    public void deauthorize(String provider) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // TODO: revoke token
        authorizedClientService.removeAuthorizedClient(provider, auth.getName());
    }

    public ActionResult linkCurrentUser(String provider) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(provider, auth.getName());

        if (auth == null || !(auth.getPrincipal() instanceof UserAuthPrincipal principle)) {
            return ActionResult.USER_UNAUTHORIZED;
        } else if (client == null) {
            return ActionResult.OAUTH_CLIENT_MISSING;
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
        USER_UNAUTHORIZED,
        OAUTH_CLIENT_MISSING,

        USER_ALREADY_LINKED,
        OAUTH_ALREADY_LINKED,
        LINK_SUCCESS,

        NOT_IMPLEMENTED,
        ;
    }
}
