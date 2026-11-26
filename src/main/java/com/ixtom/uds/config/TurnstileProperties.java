package com.ixtom.uds.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "turnstile")
public class TurnstileProperties {

    private String siteKey;
    private String secretKey;

    public String getSiteKey() {
        return siteKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSiteKey(String siteKey) {
        this.siteKey = siteKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
