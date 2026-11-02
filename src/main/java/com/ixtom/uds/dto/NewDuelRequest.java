package com.ixtom.uds.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record NewDuelRequest(
    @Pattern(regexp = YOUTUBE_URL_REGEX, message = "Invalid YouTube URL")
    String url1,

    @Pattern(regexp = YOUTUBE_URL_REGEX, message = "Invalid YouTube URL")
    String url2,

    @NotNull
    @Min(1)
    Integer round,

    @Size(max = 5000)
    String description
) {
    private static final String YOUTUBE_URL_REGEX =
        "^(https?://)?(www\\.|m\\.)?(youtube\\.com/(watch\\?.*v=|embed/|shorts/)|youtu\\.be/)[a-zA-Z0-9_-]{11}.*$";
}
