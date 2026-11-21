package com.ixtom.uds.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.ixtom.uds.config.TurnstileProperties;

@Service
public class TurnstileService {

    private static final String SITEVERIFY_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

    @Autowired
    private RestClient restClient;

    @Autowired
    private TurnstileProperties properties;

    public boolean isValidToken(String token, String remoteIp) {
        if (token == null) {
            return false;
        }

        Map<String, String> body = new HashMap<>(3);
        body.put("secret", properties.getSecretKey());
        body.put("response", token);

        if (remoteIp != null) {
            body.put("remoteip", remoteIp);
        }

        return restClient.post()
            .uri(SITEVERIFY_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .body(TurnstileResponse.class)
            .success();
    }

    record TurnstileResponse(boolean success) {}
}
