package com.ixtom.uds.service;

import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import com.ixtom.uds.dto.DiscordUserDataResponse;

@Service
@RestController
public class DiscordService { // MAYBE: make a interface for oauth info

    private static final String baseUrl = "https://discord.com/api/v10/";

    private final RestClient restClient;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    public DiscordService(RestClient restClient) {
        this.restClient = restClient.mutate().baseUrl(baseUrl).build();
    }

    // if can use api for this user, can be false even when discord is linked
    public boolean isAuthorized() {
        return authorizedClientService.loadAuthorizedClient("discord", SecurityContextHolder.getContext().getAuthentication().getName()) != null;
    }

    @GetMapping("/discord")
    public DiscordUserDataResponse getUserData() {
        return restClient
            .get()
            .uri("/users/@me")
            .attributes(clientRegistrationId("discord"))
            .retrieve()
            .body(DiscordUserDataResponse.class);
    }

    public String getExternalId() {
        return String.valueOf(getUserData().id());
    }
}
