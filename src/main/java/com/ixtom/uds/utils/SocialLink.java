package com.ixtom.uds.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum SocialLink {
    YOUTUBE_VIDEO("^(?:m\\.)?(?:youtube\\.com/(?:watch\\?.*v=|embed/|shorts/)|youtu\\.be/)([a-zA-Z0-9_-]{11})(?:[#?&].*)?$"),
    YOUTUBE_CHANNEL("^(?:m\\.)?youtube\\.com/@([a-zA-Z0-9._-]+)/?(?:[#?].*)?$"), // Maybe: allow just the handle by itself
    INSTAGRAM_PROFILE("^instagram\\.com/([a-zA-Z0-9._]+)/?(?:[#?].*)?$"),
    ;

    private final Pattern pattern;

    SocialLink(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    private static String normalizeUrl(String url) {
        if (url == null) {
            return null;
        }

        return url.strip().replaceFirst("^(?:https?://)?(?:www\\.)?", "");
    }

    public String extractId(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }

        Matcher matcher = pattern.matcher(normalizeUrl(url));

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    public boolean matches(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }

        return pattern.matcher(normalizeUrl(url)).matches();
    }
}
