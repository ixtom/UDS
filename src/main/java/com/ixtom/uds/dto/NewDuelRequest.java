package com.ixtom.uds.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record NewDuelRequest(
    @Pattern(regexp = YOUTUBE_URL_REGEX, message = "Enter a valid YouTube link, like https://www.youtube.com/watch?v=anc123XYZ_-")
    String url1,

    @Pattern(regexp = YOUTUBE_URL_REGEX, message = "Enter a valid YouTube link, like https://www.youtube.com/watch?v=anc123XYZ_-")
    String url2,

    @NotNull(message = "Enter a round number.")
    @Min(value = 1, message = "round number must be 1 or higher.")
    Integer round,

    @Size(max = 5000, message = "Description. must be 5000 characters or less.")
    String description,

    String duelBanner 
) {
    public static final String YOUTUBE_URL_REGEX =
        "^(https?://)?(www\\.|m\\.)?(youtube\\.com/(watch\\?.*v=|embed/|shorts/)|youtu\\.be/)[a-zA-Z0-9_-]{11}.*$";
}
